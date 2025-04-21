import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import java.util.Date;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/* class to demonstrate use of Calendar events list API */
public class CalendarQuickstart {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
          Collections.singletonList(CalendarScopes.CALENDAR);

  private static final String CREDENTIALS_FILE_PATH = "/home/agata/Documents/projektMiasi/src/main/resources/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */


  public static void main(String[] args) throws GeneralSecurityException, IOException, ParseException {
    System.out.println("Done");
    getUpcomingEvents(10);
//    createEvent();
//    getEventsForDay();
//    addEventBeforeAnother();
  }

  public static void getUpcomingEvents(int count) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    Calendar service = getCalendarService();

    // List the next 10 events from the primary calendar.
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



  // Metoda pobiera wydarzenia z określonego dnia
  public static void getEventsForDay(Date date) throws IOException, GeneralSecurityException {
    // Formatowanie daty do odpowiedniego formatu
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    String startOfDay = sdf.format(date);

    // Przesunięcie o 1 dzień (koniec dnia)
    Date endDate = new Date(date.getTime() + (24 * 60 * 60 * 1000) - 1);
    String endOfDay = sdf.format(endDate);

    // Określenie zakresu wydarzeń: od początku do końca dnia
    DateTime startTime = new DateTime(startOfDay);
    DateTime endTime = new DateTime(endOfDay);

    // Budowanie zapytania do Google Calendar API
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
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
          start = event.getStart().getDate();
        }

        // Wydarzenie może mieć długość
        long durationInMillis = event.getEnd().getDateTime().getValue() - start.getValue();
        long durationInMinutes = durationInMillis / (60 * 1000); // Przekształcamy czas na minuty

        // Wypisanie szczegółów wydarzenia
        System.out.println("Summary: " + summary);
        System.out.println("Location: " + (location != null ? location : "Not specified"));
        System.out.println("Start time: " + start);
        System.out.println("Duration: " + durationInMinutes + " minutes");
        System.out.println();
      }
    }
  }


  public static void createEvent(String summary, String location, String description,
                                 DateTime startDateTime, DateTime endDateTime, String colorId,
                                 boolean isRecurring) throws IOException, GeneralSecurityException {

    // Tworzymy usługę Calendar
    Calendar service = getCalendarService();

    // Tworzymy obiekt wydarzenia
    Event event = new Event()
            .setSummary(summary)
            .setLocation(location)
            .setDescription(description)
            .setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone("Europe/Warsaw"))
            .setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone("Europe/Warsaw"));

    // Ustawiamy kolor, jeśli został wybrany
    if (colorId != null && !colorId.isEmpty()) {
      event.setColorId(colorId);
    }

    // Dodajemy opcję powtarzania, jeśli wydarzenie ma się powtarzać
    if (isRecurring) {
      // Przykład reguły powtarzania: codziennie przez 5 dni
      String recurrenceRule = "RRULE:FREQ=DAILY;COUNT=5"; // Możesz zmienić na inne ustawienie powtarzania
      event.setRecurrence(Arrays.asList(recurrenceRule));
    }

    // Tworzymy wydarzenie w Google Calendar
    String calendarId = "primary"; // Twój kalendarz, "primary" to domyślny kalendarz
    event = service.events().insert(calendarId, event).execute();

    // Wypisujemy informacje o wydarzeniu
    System.out.printf("Event created: %s\n", event.getHtmlLink());
  }

  public static void addEventBeforeAnother() throws GeneralSecurityException, IOException, ParseException {
    Calendar service = getCalendarService(); // Zaimplementuj metodę getCalendarService, aby uzyskać usługę Calendar

    // Jutro
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
    Date tomorrow = calendar.getTime();

    // Dodaj wydarzenie przed wydarzeniem o zadanym summary
    addEventBeforeAnother(service, tomorrow, "Spotkanie zespołu");
  }
  public static void createNewEvent(Calendar service, DateTime startTime, String summary, String description) throws IOException {
    Event event = new Event()
            .setSummary(summary)
            .setDescription(description)
            .setStart(new EventDateTime().setDateTime(startTime).setTimeZone("UTC"))
            .setEnd(new EventDateTime().setDateTime(new DateTime(startTime.getValue() + 1000L * 60 * 30)).setTimeZone("UTC")); // 30 minut trwania

    event = service.events().insert("primary", event).execute();
    System.out.println("Event created: " + event.getSummary() + " at " + event.getStart().getDateTime());
  }

  public static void addEventBeforeAnother(Calendar service, Date day, String eventSummary) throws IOException, ParseException {
    // Przygotowanie daty początkowej (na dany dzień)
    java.util.Calendar calendar  = java.util.Calendar.getInstance();
    calendar.setTime(day);
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);

    DateTime startOfDay = new DateTime(calendar.getTime());

    // Wyszukaj wydarzenia na ten dzień
    Events events = service.events().list("primary")
            .setTimeMin(startOfDay)
            .setTimeMax(new DateTime(calendar.getTimeInMillis() + (1000L * 60 * 60 * 24))) // następny dzień
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();

    List<Event> items = events.getItems();
    Event referenceEvent = null;

    // Szukamy wydarzenia o zadanym summary
    for (Event event : items) {
      if (eventSummary.equals(event.getSummary())) {
        referenceEvent = event;
        break;
      }
    }

    // Jeśli nie znaleziono, tworzymy nowe wydarzenie na początku dnia
    if (referenceEvent == null) {
      System.out.println("No matching event found. Creating an event at the start of the day.");
      createNewEvent(service, startOfDay, "New Event", "This is your new event!");
    } else {
      // Znaleziono wydarzenie, teraz musimy dodać nowe przed tym
      DateTime referenceEventStartTime = referenceEvent.getStart().getDateTime();
      if (referenceEventStartTime == null) {
        referenceEventStartTime = referenceEvent.getStart().getDate();
      }

      // Tworzymy nowe wydarzenie na 30 minut przed znalezionym wydarzeniem
      DateTime newEventStartTime = new DateTime(referenceEventStartTime.getValue() - (1000L * 60 * 30)); // 30 minut wcześniej
      createNewEvent(service, newEventStartTime, "New Event", "This is your new event before the reference event!");
    }
  }

  //utils
  private static Calendar getCalendarService() throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Credential credential = getCredentials(HTTP_TRANSPORT); // Implementuj getCredentials, aby uzyskać poświadczenia

    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
  }

  public static void createEvent() throws GeneralSecurityException, IOException, ParseException {
    String summary = "Spotkanie z klientem";
    String location = "Pokój konferencyjny 101";
    String description = "Spotkanie w sprawie nowego projektu";
    DateTime startDateTime = new DateTime("2025-04-02T09:00:00+02:00");
    DateTime endDateTime = new DateTime("2025-04-02T10:00:00+02:00");

    // Kolor: "1" oznacza niebieski, "2" zielony, itd.
    String colorId = "1"; // Możesz podać kolor (np. "1" dla niebieskiego)

    // Powtarzanie wydarzenia (True/False)
    boolean isRecurring = true;

    // Tworzymy wydarzenie
    CalendarQuickstart.createEvent(summary, location, description, startDateTime, endDateTime, colorId, isRecurring);

  }


  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
          throws IOException {
    // Load client secrets.
    // Zmiana: zamiast getResourceAsStream, używamy FileInputStream
    File credentialsFile = new File("/home/agata/Documents/projektMiasi/src/main/resources/credentials.json");
    if (!credentialsFile.exists()) {
      throw new FileNotFoundException("Resource not found: " + credentialsFile.getAbsolutePath());
    }
    GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(credentialsFile)));


    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }
}