package logic;

import com.google.api.client.util.DateTime;
import services.CalendarService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserCalendarOperations {

    private CalendarService calendarService;

    public UserCalendarOperations() {
        calendarService = new CalendarService();
    }

    public List<String> getEventsForDay(Date date, Map<String, Boolean> options) {
        try {
            return calendarService.getEventsForDay(date, options);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listUpcomingEvents(long number) {
        try {
            return calendarService.listUpcomingEvents((int) number);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> createEventWithOptions(DateTime startDateTime,
                                         DateTime endDateTime,
                                         String summary,
                                         Map<String, Object> options) {
        try {
            return calendarService.createEventWithOptions(startDateTime, endDateTime, summary, options);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}