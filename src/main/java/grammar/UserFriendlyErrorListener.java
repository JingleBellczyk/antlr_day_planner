package grammar;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Niestandardowy listener błędów gramatyki ANTLR
 * dostarczający przyjazne dla użytkownika komunikaty błędów
 */
public class UserFriendlyErrorListener extends BaseErrorListener {

    // Mapowanie kodów błędów ANTLR na przyjazne komunikaty
    private static final Map<Integer, String> ERROR_MESSAGES = new HashMap<>();

    static {
        ERROR_MESSAGES.put(1, "Niepoprawny token");
        ERROR_MESSAGES.put(2, "Brakujący token");
        ERROR_MESSAGES.put(3, "Niepasujący token");
        ERROR_MESSAGES.put(4, "Niepasująca alternatywa");
        ERROR_MESSAGES.put(5, "Brak dopasowania");
    }

    // Mapowanie tokenów na przyjazne nazwy
    private static final Map<Integer, String> TOKEN_NAMES = new HashMap<>();

    static {
        TOKEN_NAMES.put(GrammarParser.MAIL, "mail");
        TOKEN_NAMES.put(GrammarParser.CALENDAR, "calendar");
        TOKEN_NAMES.put(GrammarParser.TASK, "task");
        TOKEN_NAMES.put(GrammarParser.TASKLIST, "tasklist");
        TOKEN_NAMES.put(GrammarParser.LIST, "list");
        TOKEN_NAMES.put(GrammarParser.CREATE, "create");
        TOKEN_NAMES.put(GrammarParser.DELETE, "delete");
        TOKEN_NAMES.put(GrammarParser.SHOW, "show");
        TOKEN_NAMES.put(GrammarParser.ALL, "all");
        TOKEN_NAMES.put(GrammarParser.EMAIL, "adres email");
        TOKEN_NAMES.put(GrammarParser.STRING, "tekst w cudzysłowach");
        TOKEN_NAMES.put(GrammarParser.DATE, "data (format: DD.MM.RRRR)");
        TOKEN_NAMES.put(GrammarParser.HOUR_MINUTE, "czas (format: HH:MM)");
        TOKEN_NAMES.put(GrammarParser.INT, "liczba");
    }

    private final List<String> errors = new ArrayList<>();
    private final String command;

    public UserFriendlyErrorListener(String command) {
        this.command = command;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {

        String errorMsg = formatErrorMessage(recognizer, offendingSymbol, charPositionInLine, e);
        errors.add(errorMsg);
    }

    /**
     * Formatuje komunikat błędu w sposób przyjazny dla użytkownika
     */
    private String formatErrorMessage(Recognizer<?, ?> recognizer,
                                      Object offendingSymbol,
                                      int charPositionInLine,
                                      RecognitionException e) {

        StringBuilder errorBuilder = new StringBuilder();

        // Jeśli mamy offendingSymbol, użyjmy go
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            String tokenText = token.getText();

            if (e instanceof InputMismatchException) {
                errorBuilder.append("Niepoprawny token '").append(tokenText).append("' na pozycji ").append(charPositionInLine);

                // Dodaj oczekiwane tokeny
                IntervalSet expectedTokens = ((InputMismatchException) e).getExpectedTokens();
                if (expectedTokens != null && !expectedTokens.isNil()) {
                    errorBuilder.append(", oczekiwano: ");
                    appendExpectedTokens(errorBuilder, recognizer, expectedTokens);
                }
            } else if (e instanceof NoViableAltException) {
                errorBuilder.append("Nie można zinterpretować '").append(tokenText)
                        .append("' w tym kontekście (pozycja ").append(charPositionInLine).append(")");
            } else {
                errorBuilder.append("Niepoprawny token '").append(tokenText).append("' na pozycji ").append(charPositionInLine);
            }
        } else if (e instanceof NoViableAltException) {
            NoViableAltException nvae = (NoViableAltException) e;
            Token startToken = nvae.getStartToken();

            if (startToken != null && startToken.getType() != Token.EOF) {
                errorBuilder.append("Nie można zinterpretować sekwencji zaczynającej się od '")
                        .append(startToken.getText()).append("'");
            } else {
                errorBuilder.append("Nie można zinterpretować komendy");
            }
        } else if (e instanceof LexerNoViableAltException) {
            CharStream input = ((LexerNoViableAltException) e).getInputStream();
            String text = input.getText(Interval.of(e.getOffendingToken().getStartIndex(), e.getOffendingToken().getStopIndex()));
            errorBuilder.append("Nierozpoznany symbol '").append(text).append("'");
        } else {
            errorBuilder.append("Błąd składni na pozycji ").append(charPositionInLine);
        }

        // Dodaj wskazówkę o typie komendy
        addCommandTypeHint(errorBuilder);

        return errorBuilder.toString();
    }

    /**
     * Dodaje wskazówkę o typie komendy na podstawie pierwszego słowa
     */
    private void addCommandTypeHint(StringBuilder builder) {
        if (command == null || command.isEmpty()) {
            return;
        }

        String[] parts = command.trim().split("\\s+", 2);
        String firstWord = parts[0].toLowerCase();

        builder.append("\n💡 Wskazówka: ");

        switch (firstWord) {
            case "mail":
                builder.append("Komendy mail wymagają operacji (show, list, create) i argumentów. Przykład: 'mail list 5'");
                break;
            case "calendar":
                builder.append("Komendy calendar wymagają operacji (show, list, create) i argumentów. Przykład: 'calendar show 01.05.2025'");
                break;
            case "task":
                builder.append("Komendy task wymagają operacji (create, show, delete) i argumentów. Przykład: 'task create \"Moja lista\" \"Nowe zadanie\"'");
                break;
            case "tasklist":
                builder.append("Komendy tasklist wymagają operacji (create, delete, rename, all). Przykład: 'tasklist all'");
                break;
            case "help":
                builder.append("Użyj 'help', 'mail help' lub 'calendar help' aby zobaczyć pomoc");
                break;
            default:
                builder.append("Podstawowe komendy to: mail, calendar, task, tasklist, help");
                break;
        }
    }

    /**
     * Dołącza listę oczekiwanych tokenów w czytelnej formie
     */
    private void appendExpectedTokens(StringBuilder builder,
                                      Recognizer<?, ?> recognizer,
                                      IntervalSet expectedTokens) {
        List<String> tokenNames = new ArrayList<>();

        for (int token : expectedTokens.toList()) {
            String tokenName = TOKEN_NAMES.getOrDefault(token, recognizer.getVocabulary().getDisplayName(token));

            // Usuń apostrofy z nazw tokenów
            if (tokenName.startsWith("'") && tokenName.endsWith("'")) {
                tokenName = tokenName.substring(1, tokenName.length() - 1);
            }

            // Zamień znaki specjalne na czytelne nazwy
            if (tokenName.equals("<EOF>")) {
                tokenName = "koniec komendy";
            } else if (tokenName.equals("STRING")) {
                tokenName = "tekst w cudzysłowach";
            }

            if (!tokenNames.contains(tokenName)) {
                tokenNames.add(tokenName);
            }
        }

        for (int i = 0; i < tokenNames.size(); i++) {
            if (i > 0) {
                if (i == tokenNames.size() - 1) {
                    builder.append(" lub ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(tokenNames.get(i));
        }
    }

    /**
     * Zwraca listę błędów
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Sprawdza, czy wystąpiły błędy
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Generuje przyjazną dla użytkownika sugestię, jak poprawić komendę
     */
    public String getSuggestion() {
        if (command == null || command.isEmpty()) {
            return "Wprowadź komendę, na przykład 'mail list 5' lub 'calendar show 01.05.2025'";
        }

        String[] parts = command.trim().split("\\s+", 3);
        String service = parts.length > 0 ? parts[0].toLowerCase() : "";
        String operation = parts.length > 1 ? parts[1].toLowerCase() : "";

        StringBuilder suggestion = new StringBuilder("Przykładowe poprawne komendy:\n");

        switch (service) {
            case "mail":
                suggestion.append("• mail list 5 - wyświetl ostatnie 5 maili\n");
                suggestion.append("• mail show 1 - pokaż szczegóły pierwszego maila\n");
                suggestion.append("• mail create user@example.com \"Temat\" \"Treść wiadomości\"");
                break;

            case "calendar":
                suggestion.append("• calendar list 10 - wyświetl 10 nadchodzących wydarzeń\n");
                suggestion.append("• calendar show 01.05.2025 - pokaż wydarzenia z danego dnia\n");
                suggestion.append("• calendar create 01.05.2025 14:30 16:30 SUMMARY: \"Spotkanie\"");
                break;

            case "task":
                suggestion.append("• task create \"Moja lista\" \"Nowe zadanie\" - utwórz nowe zadanie\n");
                suggestion.append("• task show \"Moja lista\" \"Nazwa zadania\" - pokaż szczegóły zadania\n");
                suggestion.append("• task delete \"Moja lista\" \"Nazwa zadania\" - usuń zadanie");
                break;

            case "tasklist":
                suggestion.append("• tasklist all - wyświetl wszystkie listy zadań\n");
                suggestion.append("• tasklist create \"Moja lista\" - utwórz nową listę zadań\n");
                suggestion.append("• tasklist delete \"Moja lista\" - usuń listę zadań\n");
                suggestion.append("• tasklist list \"Moja lista\" - wyświetl zadania z listy");
                break;

            default:
                suggestion.append("• mail list 5 - wyświetl ostatnie 5 maili\n");
                suggestion.append("• calendar show 01.05.2025 - pokaż wydarzenia z danego dnia\n");
                suggestion.append("• tasklist all - wyświetl wszystkie listy zadań\n");
                suggestion.append("• help - wyświetl ogólną pomoc");
                break;
        }

        return suggestion.toString();
    }
}