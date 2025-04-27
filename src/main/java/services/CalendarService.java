package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static services.Utils.durationInUnit;
import static services.Utils.formatDateTimeToString;

public class CalendarService {

    private static Calendar getCalendarService() {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return new Calendar.Builder(HTTP_TRANSPORT, Utils.JSON_FACTORY, Utils.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("PLANNER APP")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getEventsForDay(Date date, Map<String, Boolean> options) throws IOException, GeneralSecurityException {
        List<String> output = new ArrayList<>();

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
            output.add("No events found for the specified day.");
        } else {
            output.add("Events for the specified day:");
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

                output.add("Summary: " + (summary != null ? summary : "Not specified"));
                output.add("Start time: " + formatDateTimeToString(start));

                long durationInMillis = event.getEnd().getDateTime().getValue() - start.getValue();
                long durationInMinutes = durationInMillis / (60 * 1000);

                if (options.getOrDefault("description", false)) {
                    output.add("Description: " + (description != null ? description : "Not specified"));
                }
                if (options.getOrDefault("time", false)) {
                    output.add("Duration: " + durationInUnit(durationInMinutes));
                }
                if (options.getOrDefault("color", false)) {
                    output.add("Color: " + (color != null ? color : "Not specified"));
                }

                output.add("");
            }
        }

        return output;
    }

    public static List<String> listUpcomingEvents(int count) throws IOException, GeneralSecurityException {
        List<String> output = new ArrayList<>();

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
            output.add("No upcoming events found.");
        } else {
            output.add("Upcoming events:");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                output.add(event.getSummary() + " " + formatDateTimeToString(start));
            }
        }

        return output;
    }

    public List<String> createEventWithOptions(
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
            Map<String, Object> recurrenceMap = (Map<String, Object>) options.get("recurrence");
            String period = (String) recurrenceMap.get("period");
            Integer count = (Integer) recurrenceMap.get("count");

            if (period != null && count != null) {
                String rule = String.format("RRULE:FREQ=%s;COUNT=%d", period.toUpperCase(), count);
                event.setRecurrence(Collections.singletonList(rule));
            }
        }

        if (options.containsKey("before")) {
            String beforeSummary = (String) options.get("before");

            DateTime newEndTime = findStartOfEventAfter(service, startDateTime, beforeSummary);
            if (newEndTime != null && newEndTime.getValue() < endDateTime.getValue()) {
                long durationMillis = endDateTime.getValue() - startDateTime.getValue();
                DateTime newStartDate = new DateTime(newEndTime.getValue() - durationMillis);

                event.setStart(new EventDateTime().setDateTime(newStartDate).setTimeZone("Europe/Warsaw"));
                event.setEnd(new EventDateTime().setDateTime(newEndTime).setTimeZone("Europe/Warsaw"));
            }

        }

        List<String> output = new ArrayList<>();

        try {
            Event createdEvent = service.events().insert("primary", event).execute();
            output.add("Event created: " + createdEvent.getHtmlLink());
        } catch (GoogleJsonResponseException e) {

            String errorMessage = e.getDetails() != null && e.getDetails().containsKey("message")
                    ? (String) e.getDetails().get("message")
                    : "Unknown error";

            output.add("Error: " + errorMessage);
        }

        return output;
    }

    private static DateTime findStartOfEventAfter(Calendar service, DateTime referenceDate, String summary) throws IOException {

        Instant instant = Instant.ofEpochMilli(referenceDate.getValue());
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();

        // start dnia
        LocalDateTime startOfDay = localDate.atStartOfDay();
        DateTime timeMin = new DateTime(startOfDay.atZone(zoneId).toInstant().toEpochMilli());

        // koniec dnia
        LocalDateTime startOfNextDay = localDate.plusDays(1).atStartOfDay();
        DateTime timeMax = new DateTime(startOfNextDay.atZone(zoneId).toInstant().toEpochMilli());

        Events events = service.events().list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        for (Event event : events.getItems()) {
            if (summary.equals(event.getSummary())) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) start = event.getStart().getDate();
                return new DateTime(start.getValue());
            }
        }
        return null;
    }
}
