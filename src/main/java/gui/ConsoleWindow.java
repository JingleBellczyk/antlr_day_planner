package gui;

import interpreter.Start;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;

/**
 * Ulepszony interfejs konsolowy z lepszą obsługą informacji zwrotnej dla użytkownika
 */
public class ConsoleWindow extends JFrame {

    private final JTextPane outputArea; // Zmiana na JTextPane dla lepszego formatowania
    private final JTextField inputField;
    private final java.util.List<String> commandHistory = new java.util.ArrayList<>();
    private int historyIndex = -1;

    // Style dla różnych typów komunikatów
    private final javax.swing.text.Style normalStyle;
    private final javax.swing.text.Style errorStyle;
    private final javax.swing.text.Style successStyle;
    private final javax.swing.text.Style commandStyle;
    private final javax.swing.text.Style tipStyle;

    public ConsoleWindow() {
        setTitle("Simple Day Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Górny panel z logo i przyciskami
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(66, 133, 244)); // Google blue

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/logo.png")));
        Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(image));

        JLabel titleText = new JLabel("Simple Day Planner");
        titleText.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleText.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(logoLabel);
        titlePanel.add(titleText);

        JPanel linkPanel = getQuickAccessPanel();

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(linkPanel, BorderLayout.EAST);

        // Obszar wyjściowy z formatowaniem tekstu
        outputArea = new JTextPane();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Inicjalizacja stylów tekstu
        javax.swing.text.StyledDocument doc = outputArea.getStyledDocument();
        normalStyle = doc.addStyle("normal", null);
        errorStyle = doc.addStyle("error", null);
        successStyle = doc.addStyle("success", null);
        commandStyle = doc.addStyle("command", null);
        tipStyle = doc.addStyle("tip", null);

        // Ustawienie kolorów dla stylów
        javax.swing.text.StyleConstants.setForeground(normalStyle, new Color(50, 50, 50));
        javax.swing.text.StyleConstants.setForeground(errorStyle, new Color(200, 0, 0));
        javax.swing.text.StyleConstants.setForeground(successStyle, new Color(0, 150, 0));
        javax.swing.text.StyleConstants.setForeground(commandStyle, new Color(0, 0, 200));
        javax.swing.text.StyleConstants.setForeground(tipStyle, new Color(120, 90, 0));

        // Dodatkowe formatowanie
        javax.swing.text.StyleConstants.setBold(errorStyle, true);
        javax.swing.text.StyleConstants.setBold(successStyle, true);
        javax.swing.text.StyleConstants.setItalic(tipStyle, true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel dolny z inputem, przyciskiem pliku i pomocą
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Pole wprowadzania komend
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));

        // Przyciski akcji
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton fileButton = createFileButton();
        JButton executeButton = new JButton("Wykonaj");
        executeButton.addActionListener((ActionEvent e) -> executeCommand());
        JButton helpButton = new JButton("?");
        helpButton.setToolTipText("Pokaż pomoc");
        helpButton.addActionListener(e -> {
            showHelp();
        });

        buttonPanel.add(fileButton);
        buttonPanel.add(executeButton);
        buttonPanel.add(helpButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Panel szablonów komend
        JPanel templatesPanel = createTemplatesPanel();

        bottomPanel.add(templatesPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        // Obsługa wprowadzania komendy przez Enter
        inputField.addActionListener((ActionEvent e) -> executeCommand());
        setupHistoryNavigation();

        // Złożenie interfejsu
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Początkowy komunikat powitalny
        appendFormattedText("Witaj w Simple Day Planner!\n", normalStyle);
        appendFormattedText("Wprowadź komendę lub wpisz 'help' aby uzyskać pomoc.\n", normalStyle);
        appendFormattedText("Możesz szybko rozpocząć korzystając z szablonów komend poniżej.\n\n", normalStyle);

        setVisible(true);
    }

    public ConsoleWindow(JTextPane outputArea, JTextField inputField, javax.swing.text.Style normalStyle, javax.swing.text.Style errorStyle, javax.swing.text.Style successStyle, javax.swing.text.Style commandStyle, javax.swing.text.Style tipStyle) {
        this.outputArea = outputArea;
        this.inputField = inputField;
        this.normalStyle = normalStyle;
        this.errorStyle = errorStyle;
        this.successStyle = successStyle;
        this.commandStyle = commandStyle;
        this.tipStyle = tipStyle;
    }

    private JPanel getQuickAccessPanel() {
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        linkPanel.setOpaque(false);

        for (String service : new String[]{"Gmail", "Calendar", "Tasks"}) {
            JButton button = new JButton(service);
            button.setBackground(new Color(255, 255, 255));
            button.setForeground(new Color(66, 133, 244));
            button.setFocusPainted(false);
            button.addActionListener(e -> openLink("https://" + service.toLowerCase() + ".google.com"));
            linkPanel.add(button);
        }

        return linkPanel;
    }

    private JPanel createTemplatesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Szablony komend"));

        String[][] templates = {
                {"📧 Pokaż mail", "mail show 1"},
                {"📧 Lista maili", "mail list 5"},
                {"📅 Lista wydarzeń", "calendar list 5"},
                {"📅 Pokaż wydarzenia", "calendar show 01.05.2025"},
                {"✓ Lista zadań", "tasklist all"}
        };

        for (String[] template : templates) {
            JButton button = new JButton(template[0]);
            button.addActionListener(e -> {
                inputField.setText(template[1]);
                inputField.requestFocus();
            });
            panel.add(button);
        }

        return panel;
    }

    private JButton createFileButton() {
        JButton fileButton = new JButton("📂");
        fileButton.setToolTipText("Dodaj plik");
        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String currentText = inputField.getText();
                String path = selectedFile.getAbsolutePath().replace("\\", "\\\\");
                inputField.setText(currentText + " \"" + path + "\"");
                inputField.requestFocus();
            }
        });
        return fileButton;
    }

    private void executeCommand() {
        String command = inputField.getText().trim();
        if (command.isEmpty()) return;

        appendFormattedText("\n> " + command + "\n", commandStyle);
        commandHistory.add(command);
        historyIndex = commandHistory.size();
        inputField.setText("");

        // Ustawienie kontekstu komendy dla lepszych komunikatów błędów
        interpreter.Start.setCommandContext(command);

        try {
            // Przetwarzanie komendy
            List<String> results = processCommand(command);

            // Formatowanie wyników
            if (results != null && !results.isEmpty()) {
                for (String result : results) {
                    formatAndAppendResult(result);
                }
            } else {
                appendFormattedText("Komenda wykonana bez wyników.\n", normalStyle);
            }
        } catch (Exception ex) {
            appendFormattedText("Błąd: " + ex.getMessage() + "\n", errorStyle);
            appendFormattedText("Wpisz 'help' aby uzyskać pomoc dotyczącą poprawnego formatu komend.\n", tipStyle);
        }
    }

    private void formatAndAppendResult(String result) {
        if (result == null) return;

        // Wykrywanie typu komunikatu na podstawie charakterystycznych wzorców
        if (result.trim().isEmpty()) {
            appendFormattedText("\n", normalStyle);
            return;
        }

        // Rozpoznanie komunikatów błędów
        if (result.startsWith("❌") ||
                result.startsWith("Błąd:") ||
                result.startsWith("ERROR:") ||
                result.toLowerCase().contains("failed") ||
                result.toLowerCase().contains("error")) {

            appendFormattedText(result + "\n", errorStyle);
            return;
        }

        // Rozpoznanie podpowiedzi
        if (result.startsWith("💡") ||
                result.startsWith("   •") ||
                result.startsWith("ℹ️")) {

            appendFormattedText(result + "\n", tipStyle);
            return;
        }

        // Rozpoznanie sukcesów
        if (result.startsWith("✅") ||
                result.toLowerCase().contains("created") ||
                result.toLowerCase().contains("success") ||
                result.toLowerCase().contains("sent") ||
                result.toLowerCase().contains("updated")) {

            appendFormattedText(result + "\n", successStyle);
            return;
        }

        // Standardowe wyniki
        appendFormattedText(result + "\n", normalStyle);
    }

    private void appendFormattedText(String text, javax.swing.text.Style style) {
        javax.swing.text.StyledDocument doc = outputArea.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
            outputArea.setCaretPosition(doc.getLength());
        } catch (javax.swing.text.BadLocationException e) {
            System.err.println("Nie udało się dodać tekstu: " + e.getMessage());
        }
    }

    private void setupHistoryNavigation() {
        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_UP:
                        navigateHistory(-1);
                        break;
                    case java.awt.event.KeyEvent.VK_DOWN:
                        navigateHistory(1);
                        break;
                    case java.awt.event.KeyEvent.VK_TAB:
                        autocompleteCommand();
                        e.consume();
                        break;
                }
            }
        });
    }

    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) return;

        if (direction < 0 && historyIndex > 0) { // UP
            historyIndex--;
            inputField.setText(commandHistory.get(historyIndex));
        } else if (direction > 0) { // DOWN
            if (historyIndex < commandHistory.size() - 1) {
                historyIndex++;
                inputField.setText(commandHistory.get(historyIndex));
            } else {
                historyIndex = commandHistory.size();
                inputField.setText("");
            }
        }
    }

    private void autocompleteCommand() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        // Podstawowe sugestie komend
        String[][] suggestions = {
                {"m", "mail list 5"},
                {"ma", "mail list 5"},
                {"mail l", "mail list "},
                {"mail s", "mail show "},
                {"mail c", "mail create "},
                {"c", "calendar list 5"},
                {"ca", "calendar list 5"},
                {"cal", "calendar list 5"},
                {"calendar l", "calendar list "},
                {"calendar s", "calendar show "},
                {"calendar c", "calendar create "},
                {"t", "tasklist all"},
                {"ta", "tasklist all"},
                {"tasklist c", "tasklist create "},
                {"task c", "task create "}
        };

        for (String[] suggestion : suggestions) {
            if (text.startsWith(suggestion[0]) && !text.equals(suggestion[1])) {
                inputField.setText(suggestion[1]);
                return;
            }
        }
    }

    private List<String> processCommand(String command) {
        try {
            return Start.start(command);
        } catch (Exception ex) {
            List<String> error = new java.util.ArrayList<>();
            error.add("❌ Błąd podczas przetwarzania komendy: " + ex.getMessage());
            return error;
        }
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            appendFormattedText("Nie udało się otworzyć strony: " + url + "\n", errorStyle);
        }
    }

    private void showHelp() {
        // Wywołanie komendy help
        List<String> helpResults = processCommand("help");

        // Jeśli help zwraca wyniki, wyświetl je
        if (helpResults != null && !helpResults.isEmpty()) {
            appendFormattedText("\n--- POMOC ---\n", commandStyle);
            for (String result : helpResults) {
                appendFormattedText(result + "\n", normalStyle);
            }
        } else {
            // Podstawowa pomoc jeśli komenda help nie działa
            appendFormattedText("\n--- PODSTAWOWA POMOC ---\n", commandStyle);
            appendFormattedText("Dostępne komendy:\n", normalStyle);
            appendFormattedText("• mail list N - wyświetl ostatnie N maili\n", normalStyle);
            appendFormattedText("• mail show N - pokaż szczegóły maila o indeksie N\n", normalStyle);
            appendFormattedText("• mail create EMAIL \"TEMAT\" \"TREŚĆ\" - wyślij nowy email\n", normalStyle);
            appendFormattedText("• calendar list N - wyświetl nadchodzące N wydarzeń\n", normalStyle);
            appendFormattedText("• calendar show DD.MM.RRRR - pokaż wydarzenia z danego dnia\n", normalStyle);
            appendFormattedText("• tasklist all - wyświetl wszystkie listy zadań\n", normalStyle);
            appendFormattedText("• task create \"LISTA\" \"NAZWA\" - utwórz nowe zadanie\n", normalStyle);
            appendFormattedText("\nSzczegółowa pomoc dla poszczególnych serwisów:\n", normalStyle);
            appendFormattedText("• mail help\n• calendar help\n• task help\n", normalStyle);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(ConsoleWindow::new);
    }
}