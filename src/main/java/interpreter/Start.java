package interpreter;

import grammar.GrammarLexer;
import grammar.GrammarParser;
import logic.ErrorHandler;
import logic.ErrorHandler.ErrorCategory;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Główna klasa pośrednicząca między interfejsem użytkownika a logiką aplikacji.
 * Odpowiada za przetwarzanie komend i obsługę błędów.
 */
public class Start {
    private static final Logger LOGGER = Logger.getLogger(Start.class.getName());

    /**
     * Punkt wejścia do przetwarzania komend użytkownika
     * @param command Komenda wprowadzona przez użytkownika
     * @return Lista komunikatów zwrotnych
     */
    public static List<String> start(String command) {
        List<String> response = new ArrayList<>();

        try {
            // Walidacja danych wejściowych
            if (command == null || command.trim().isEmpty()) {
                response.add("Proszę wprowadzić komendę. Wpisz 'help' aby uzyskać pomoc.");
                return response;
            }

            LOGGER.info("Przetwarzanie komendy: " + command);

            // Przetwarzanie komendy
            CharStream charStream = CharStreams.fromString(command);
            ErrorCollectingLexer lexer = new ErrorCollectingLexer(charStream);

            // Ustawienie niestandardowego handlera dla błędów leksera
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    throw new ParseCancellationException("Błąd składni w pozycji " + charPositionInLine + ": " + msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GrammarParser parser = new GrammarParser(tokens);

            // Ustawienie niestandardowego handlera dla błędów parsera
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    throw new ParseCancellationException("Błąd komendy w pozycji " + charPositionInLine + ": " + msg);
                }
            });

            // Sprawdzenie błędów leksera
            if (!lexer.getErrors().isEmpty()) {
                response.add("❌ Błąd składni: " + lexer.getErrors().get(0));
                response.add("ℹ️ Wpisz 'help' aby zobaczyć poprawną składnię komend.");
                return response;
            }

            // Utworzenie wizytatora i wykonanie komendy
            PlannerVisitor visitor = new PlannerVisitor(charStream, tokens);
            response = visitor.visit(parser.prog());

            if (response == null || response.isEmpty()) {
                response = new ArrayList<>();
                response.add("✅ Komenda wykonana pomyślnie, ale nie zwróciła żadnych danych.");
            }

            return response;

        } catch (ParseCancellationException e) {
            // Błędy składni i parsowania
            LOGGER.log(Level.WARNING, "Błąd parsowania komendy", e);
            return formatUserFriendlyError(e, "Niepoprawna składnia komendy", ErrorCategory.PARSING);

        } catch (IllegalArgumentException e) {
            // Błędy walidacji argumentów
            LOGGER.log(Level.WARNING, "Błąd walidacji argumentów", e);
            return formatUserFriendlyError(e, "Niepoprawne argumenty", ErrorCategory.VALIDATION);

        } catch (Exception e) {
            // Pozostałe błędy
            LOGGER.log(Level.SEVERE, "Nieoczekiwany błąd podczas przetwarzania komendy", e);
            return ErrorHandler.handleException(e, "Przetwarzanie komendy");
        }
    }

    /**
     * Formatuje błąd w przyjazny dla użytkownika sposób
     */
    private static List<String> formatUserFriendlyError(Exception e, String context, ErrorCategory category) {
        List<String> errorMessages = new ArrayList<>();

        // Nagłówek błędu
        errorMessages.add("❌ " + context + ":");

        // Treść błędu
        String message = e.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            errorMessages.add("   " + message);
        } else {
            errorMessages.add("   Wystąpił nieoczekiwany błąd podczas przetwarzania komendy.");
        }

        // Sugestie naprawy
        errorMessages.add("");
        errorMessages.add("💡 Sugestie:");

        switch (category) {
            case PARSING:
                errorMessages.add("   • Sprawdź poprawność składni komendy");
                errorMessages.add("   • Upewnij się, że wszystkie cudzysłowy są prawidłowo zamknięte");
                errorMessages.add("   • Wpisz 'help' aby zobaczyć dostępne komendy");
                if (command.toLowerCase().contains("mail")) {
                    errorMessages.add("   • Wpisz 'mail help' aby zobaczyć składnię komend e-mail");
                } else if (command.toLowerCase().contains("calendar")) {
                    errorMessages.add("   • Wpisz 'calendar help' aby zobaczyć składnię komend kalendarza");
                } else if (command.toLowerCase().contains("task")) {
                    errorMessages.add("   • Wpisz 'tasklist help' aby zobaczyć składnię komend zadań");
                }
                break;

            case VALIDATION:
                errorMessages.add("   • Upewnij się, że daty są w formacie DD.MM.YYYY");
                errorMessages.add("   • Upewnij się, że godziny są w formacie HH:MM");
                errorMessages.add("   • Teksty muszą być w cudzysłowach, np. \"Mój temat\"");
                break;

            default:
                errorMessages.add("   • Spróbuj uprościć komendę");
                errorMessages.add("   • Sprawdź 'help' aby zobaczyć poprawną składnię");
        }

        return errorMessages;
    }

    /**
     * Niestandardowy lekser zbierający błędy
     */
    private static class ErrorCollectingLexer extends GrammarLexer {
        private final List<String> errors = new ArrayList<>();

        public ErrorCollectingLexer(CharStream input) {
            super(input);
        }

        @Override
        public void notifyListeners(LexerNoViableAltException e) {
            String text = this._input.getText(Interval.of(this._tokenStartCharIndex, this._input.index()));
            String msg = "Nierozpoznany token: '" + this.getErrorDisplay(text) + "'";
            errors.add(msg);
            super.notifyListeners(e);
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Pomocnicza zmienna dla formatUserFriendlyError
     */
    private static String command;

    /**
     * Ustawia kontekst komendy dla lepszych komunikatów błędów
     */
    public static void setCommandContext(String cmd) {
        command = cmd;
    }
}