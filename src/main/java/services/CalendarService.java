package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static services.Utils.durationInUnit;

public class CalendarService {


    private static Calendar getCalendarService() {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return new Calendar.Builder(HTTP_TRANSPORT, Utils.JSON_FACTORY, Utils.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("Czy to potrzebne")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void getEventsForDay(Date date, Map<String, Boolean> options) throws IOException, GeneralSecurityException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startOfDay = sdf.format(date);
        Date endDate = new Date(date.getTime() + (24 * 60 * 60 * 1000) - 1);
        String endOfDay = sdf.format(endDate);

        DateTime startTime = new DateTime(startOfDay);
        DateTime endTime = new DateTime(endOfDay);

        Calendar service = getCalendarService();
        Events events = service.events().list("primary")
                .setTimeMin(startTime)
                .setTimeMax(endTime)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();

        if (items.isEmpty()) {
            System.out.println("No events found for the specified day.");
        } else {
            System.out.println("Events for the specified day:");
            for (Event event : items) {
                String location = event.getLocation();
                String summary = event.getSummary();
                String description = event.getDescription();
                DateTime start = event.getStart().getDateTime();
                String colorId = event.getColorId();
                String color = (colorId != null) ? CalendarColor.getNameById(colorId) : null;

                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.println("Summary: " + (summary != null ? summary : "Not specified"));

                System.out.println("Start time: " + start);

                long durationInMillis = event.getEnd().getDateTime().getValue() - start.getValue();
                long durationInMinutes = durationInMillis / (60 * 1000);

                if (options.getOrDefault("description", false)) {
                    System.out.println("Description: " + (description != null ? description : "Not specified"));
                }
                if (options.getOrDefault("time", false)) {
                    System.out.println("Duration: " + durationInUnit(durationInMinutes) + " minutes");
                }
                if (options.getOrDefault("color", false)) {
                    System.out.println("Color: " + (color != null ? color : "Not specified"));
                }

                System.out.println();
            }
        }
    }
    public static void listUpcomingEvents(int count) throws IOException, GeneralSecurityException {

        Calendar service = getCalendarService();

        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(count)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }

    public void createEventWithOptions(
            DateTime startDateTime,
            DateTime endDateTime,
            String summary,
            Map<String, Object> options
    ) throws IOException, GeneralSecurityException {

        Calendar service = getCalendarService();

        Event event = new Event()
                .setSummary(summary)
                .setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone("Europe/Warsaw"))
                .setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone("Europe/Warsaw"));

        // OPCJONALNE POLA Z MAPY
        if (options.containsKey("location")) {
            event.setLocation((String) options.get("location"));
        }

        if (options.containsKey("description")) {
            event.setDescription((String) options.get("description"));
        }

        if (options.containsKey("color")) {
            String colorName = (String) options.get("color");
            String colorId = CalendarColor.getIdByName(colorName);
            if (colorId != null) {
                event.setColorId(colorId);
            }
        }

        if (options.containsKey("recurrence")) {
            // Wartość: Map<String, Object> zawierająca: "period", "count"
            Map<String, Object> recurrenceMap = (Map<String, Object>) options.get("recurrence");
            String period = (String) recurrenceMap.get("period");
            Integer count = (Integer) recurrenceMap.get("count");

            if (period != null && count != null) {
                String rule = String.format("RRULE:FREQ=%s;COUNT=%d", period.toUpperCase(), count);
                event.setRecurrence(Collections.singletonList(rule));
            }
        }

        if (options.containsKey("before")) {
            // DODAWANIE PRZED INNYM EVENTEM
            String beforeSummary = (String) options.get("before");

            // Szukamy wydarzenia o podanym summary
            DateTime newTime = findTimeBeforeEvent(service, startDateTime, beforeSummary);
            if (newTime != null) {
                // Zamieniamy czas startowy/końcowy na 30 min przed tamtym wydarzeniem
                startDateTime = newTime;
                endDateTime = new DateTime(newTime.getValue() + 30 * 60 * 1000);
                event.setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone("Europe/Warsaw"));
                event.setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone("Europe/Warsaw"));
            }
        }

        event = service.events().insert("primary", event).execute();
        System.out.println("Event created: " + event.getHtmlLink());
    }

    private static DateTime findTimeBeforeEvent(Calendar service, DateTime referenceDate, String summary) throws IOException {
        Events events = service.events().list("primary")
                .setTimeMin(referenceDate)
                .setTimeMax(new DateTime(referenceDate.getValue() + (1000L * 60 * 60 * 24)))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        for (Event event : events.getItems()) {
            if (summary.equals(event.getSummary())) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) start = event.getStart().getDate();
                return new DateTime(start.getValue() - 30 * 60 * 1000);
            }
        }
        return null;
    }

}
