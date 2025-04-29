package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa obsługująca komendy pomocy dla różnych serwisów
 */
public class HelpManager {
    private static final Logger LOGGER = Logger.getLogger(HelpManager.class.getName());

    // Ścieżki do plików pomocy (w resources)
    private static final String HELP_GENERAL_PATH = "/help/help.txt";
    private static final String HELP_MAIL_PATH = "/help/help_mail.txt";
    private static final String HELP_CALENDAR_PATH = "/help/help_calendar.txt";
    private static final String HELP_TASKS_PATH = "/help/help_tasks.txt";

    /**
     * Zwraca zawartość pliku pomocy ogólnej
     * @return Lista linii z pliku pomocy
     */
    public static List<String> getGeneralHelp() {
        return readHelpFile(HELP_GENERAL_PATH);
    }

    /**
     * Zwraca zawartość pliku pomocy dla maila
     * @return Lista linii z pliku pomocy dla maila
     */
    public static List<String> getMailHelp() {
        return readHelpFile(HELP_MAIL_PATH);
    }

    /**
     * Zwraca zawartość pliku pomocy dla kalendarza
     * @return Lista linii z pliku pomocy dla kalendarza
     */
    public static List<String> getCalendarHelp() {
        return readHelpFile(HELP_CALENDAR_PATH);
    }

    /**
     * Zwraca zawartość pliku pomocy dla zadań
     * @return Lista linii z pliku pomocy dla zadań
     */
    public static List<String> getTasksHelp() {
        return readHelpFile(HELP_TASKS_PATH);
    }

    /**
     * Odczytuje plik pomocy z zasobów
     * @param path Ścieżka do pliku pomocy
     * @return Lista linii z pliku pomocy
     */
    private static List<String> readHelpFile(String path) {
        List<String> helpLines = new ArrayList<>();

        try (InputStream is = HelpManager.class.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                helpLines.add(line);
            }

        } catch (IOException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Nie można odczytać pliku pomocy: " + path, e);
            helpLines.add("❌ Nie można załadować pliku pomocy: " + path);
            helpLines.add("Prosimy o kontakt z administratorem systemu.");
        }

        return helpLines;
    }

    /**
     * Alternatywna implementacja odczytu pliku pomocy z dysku
     * @param filePath Pełna ścieżka do pliku pomocy
     * @return Lista linii z pliku pomocy
     */
    public static List<String> readHelpFromDisk(String filePath) {
        List<String> helpLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new java.io.FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                helpLines.add(line);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Nie można odczytać pliku pomocy: " + filePath, e);
            helpLines.add("❌ Nie można załadować pliku pomocy: " + filePath);
            helpLines.add("Prosimy o kontakt z administratorem systemu.");
        }

        return helpLines;
    }
}