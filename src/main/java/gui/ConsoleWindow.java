package gui;

import interpreter.Start;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class ConsoleWindow extends JFrame {

    private JTextArea outputArea;
    private JTextField inputField;
    private java.util.List<String> commandHistory = new java.util.ArrayList<>();
    private int historyIndex = -1;

    public ConsoleWindow() {
        setTitle("Simple Day Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ----------------------
        // Górny panel z logo i przyciskami
        // ----------------------
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(240, 240, 240));

        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png")); // ścieżka do pliku w resources
        Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(image));

        JLabel titleText = new JLabel("Simple Day Planner");
        titleText.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(logoLabel);
        titlePanel.add(titleText);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton gmailButton = new JButton("GMAIL");
        JButton calendarButton = new JButton("CALENDAR");
        JButton tasksButton = new JButton("TASKS");

        gmailButton.addActionListener(e -> openLink("https://mail.google.com"));
        calendarButton.addActionListener(e -> openLink("https://calendar.google.com"));
        tasksButton.addActionListener(e -> openLink("https://tasks.google.com"));

        linkPanel.add(gmailButton);
        linkPanel.add(calendarButton);
        linkPanel.add(tasksButton);
        linkPanel.setOpaque(false);

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(linkPanel, BorderLayout.EAST);

        // ----------------------
        // Obszar wyjściowy (scrollowalny)
        // ----------------------
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // ----------------------
        // Panel dolny z inputem i przyciskiem pliku
        // ----------------------
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));

        JButton fileButton = new JButton("📂 Add file");
        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String currentText = inputField.getText();
                String path = "\"" + selectedFile.getAbsolutePath() + "\"";
                inputField.setText(currentText + " " + path);
                inputField.requestFocus();
            }
        });

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(fileButton, BorderLayout.EAST);

        // ----------------------
        // Obsługa inputu
        // ----------------------
        inputField.addActionListener((ActionEvent e) -> {
            String command = inputField.getText();
            appendOutput("> " + command);
            commandHistory.add(command);
            historyIndex = commandHistory.size();
            inputField.setText("");

            // Tu dodaj własną obsługę komendy
            try {
                String result = processCommand(command);
                appendOutput(result);
            } catch (Exception ex) {
                appendOutput("Błąd: " + ex.getMessage());
            }
        });

        //mozna strzalki do
        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                    if (historyIndex > 0) {
                        historyIndex--;
                        inputField.setText(commandHistory.get(historyIndex));
                    }
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    if (historyIndex < commandHistory.size() - 1) {
                        historyIndex++;
                        inputField.setText(commandHistory.get(historyIndex));
                    } else {
                        // Po ostatniej komendzie pokaż pusty input
                        historyIndex = commandHistory.size();
                        inputField.setText("");
                    }
                }
            }
        });

        // ----------------------
        // Składanie całości
        // ----------------------
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void appendOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private String processCommand(String command) {
        // Tutaj logika Twoich komend
        try {
            // Wywołanie Start.start(command), które zwróci List<String>
            List<String> resultList = Start.start(command);

            // Łączenie wyników w jedną wiadomość (jeśli są różne linie)
            StringBuilder resultBuilder = new StringBuilder();
            for (String result : resultList) {
                resultBuilder.append(result).append("\n");
            }

            // Zwracamy wynik, który zostanie przekazany do appendOutput
            return resultBuilder.toString();
        } catch (Exception ex) {
            // Obsługuje błąd i zwraca komunikat o błędzie
            return "Błąd: " + ex.getMessage();
        }
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            appendOutput("Nie udało się otworzyć strony: " + url);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConsoleWindow::new);
    }
}
