package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private static String credentialsPath = "src/main/resources/credentials.json";

    private static final List<String> SCOPES = Stream.concat(
            Arrays.asList(
                    "https://www.googleapis.com/auth/gmail.readonly",
                    "https://www.googleapis.com/auth/gmail.send"
            ).stream(),
            Collections.singletonList(CalendarScopes.CALENDAR).stream()
    ).collect(Collectors.toList());
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.

        File credentialsFile = new File(credentialsPath);
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

    public static String stripQuotes(String text) {
        if (text != null && text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    public static Date parseAndValidateDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        sdf.setLenient(false);
        Date date;

        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            date = null;
        }
        return date;
    }

    public static LocalTime parseAndValidateTime(String timeString) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm"); // pozwala na 0:05, 09:00, 23:59 itd.

        try {
            return LocalTime.parse(timeString, timeFormatter);
        } catch (DateTimeParseException e) { //todo
            System.out.printf("Bad time format: %s, required format HH:mm (0-23:00-59)%n", timeString);
            return null;
        }
    }

    public static DateTime mergeDateAndTime(Date date, LocalTime time) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Date combined = Date.from(
                time.atDate(localDate)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        return new DateTime(combined);
    }

    public static String durationInUnit(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else if (minutes < 1440) { // mniej niż 1 dzień
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return (remainingMinutes > 0)
                    ? String.format("%d h %d min", hours, remainingMinutes)
                    : String.format("%d h", hours);
        } else {
            long days = minutes / 1440;
            long remaining = minutes % 1440;
            long hours = remaining / 60;
            long remainingMinutes = remaining % 60;

            StringBuilder result = new StringBuilder();
            result.append(days).append(" d");
            if (hours > 0) {
                result.append(" ").append(hours).append(" h");
            }
            if (remainingMinutes > 0) {
                result.append(" ").append(remainingMinutes).append(" min");
            }
            return result.toString();
        }
    }

    public static List<String> readFileTxt(String path) {
        List<String> commands = new ArrayList<>();

        try {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);

            if (inputStream == null) {
                commands.add("Nie znaleziono pliku z komendami.");
                return commands;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                commands.add(line);
            }

            reader.close();
        } catch (IOException e) {
            commands.add("Błąd podczas wczytywania komend: " + e.getMessage());
        }

        return commands;
    }
}
