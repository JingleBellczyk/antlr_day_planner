package logic;

import com.google.api.client.util.DateTime;
import services.CalendarService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

public class UserCalendarOperations {

    private CalendarService calendarService;

    public UserCalendarOperations() {
        calendarService = new CalendarService();
    }

    public void getEventsForDay(Date date, Map<String, Boolean> options) {
        try {
            calendarService.getEventsForDay(date, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public void listUpcomingEvents(long number) {
        try {
            calendarService.listUpcomingEvents((int) number);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public void createEventWithOptions(DateTime startDateTime,
                                       DateTime endDateTime,
                                       String summary,
                                       Map<String, Object> options) {
        try {
            calendarService.createEventWithOptions(startDateTime,endDateTime,summary,options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
