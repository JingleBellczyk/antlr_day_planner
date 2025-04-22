package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class MailService {

    private static List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.send"
    );


    private Gmail service;

    public MailService(){
        this.service = getGoogleService();
    }

    public static Gmail getGoogleService() {

        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gmail service = null;
        try {
            service = new Gmail.Builder(HTTP_TRANSPORT, Utils.JSON_FACTORY, Utils.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("Czy to potrzebne")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return service;
    }

    public List<Message> getRecentEmails(Long count) {

        // Pobierz listę wiadomości (do ostatnich 10)
        ListMessagesResponse messagesResponse = null;
        try {
            messagesResponse = service.users().messages().list("me")
                    .setMaxResults(count)  // Maksymalna liczba wiadomości
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Sprawdź, czy są wiadomości
        if (messagesResponse.getMessages() == null || messagesResponse.getMessages().isEmpty()) {
            System.out.println("Brak wiadomości.");
            return null;
        } else {
            System.out.println("Ostatnie " + count + " wiadomości:");

            // Iteruj przez ID wiadomości i pobierz temat każdej z nich
            for (Message message : messagesResponse.getMessages()) {
                // Pobierz pełne szczegóły wiadomości
                Message fullMessage = null;
                try {
                    fullMessage = service.users().messages().get("me", message.getId()).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Sprawdź nagłówki wiadomości, aby znaleźć temat
                String subject = "Brak tematu";
                for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                    if ("Subject".equalsIgnoreCase(header.getName())) {
                        subject = header.getValue();
                        break;
                    }
                }

                // Wyświetl temat wiadomości
                System.out.println("Temat: " + subject);
            }
        }
        return messagesResponse.getMessages();
    }

    public void getEmailByIndex(int index){
        // Pobierz listę wiadomości
        long count = 10;
        ListMessagesResponse messagesResponse = null;
        try {
            messagesResponse = service.users().messages().list("me")
                    .setMaxResults(count)  // Możesz dostosować liczbę wiadomości, które chcesz pobrać
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Sprawdź, czy są wiadomości
        if (messagesResponse.getMessages() == null || messagesResponse.getMessages().isEmpty()) {
            System.out.println("Brak wiadomości.");
        } else {
            // Sprawdź, czy indeks jest prawidłowy
            if (index < 0 || index >= messagesResponse.getMessages().size()) {
                System.out.println("Indeks poza zakresem.");
            } else {
                // Pobierz ID wiadomości na podstawie indeksu
                Message message = messagesResponse.getMessages().get(index);

                // Pobierz pełne szczegóły wiadomości
                Message fullMessage = null;
                try {
                    fullMessage = service.users().messages().get("me", message.getId()).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Wyświetl informacje o wiadomości (np. temat)
                System.out.println("Temat wiadomości:");
                String subject = "Brak tematu";
                for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                    if ("Subject".equalsIgnoreCase(header.getName())) {
                        subject = header.getValue();
                        break;
                    }
                }
                System.out.println("Temat: " + subject);

                // Wyświetl treść wiadomości (jeśli dostępna)
                System.out.println("Treść wiadomości:");
                String body = null;
                try {
                    body = getBody(fullMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(body);
            }
        }
    }

    private static String getBody(Message message) throws IOException {
        String body = "";
        if (message.getPayload() != null && message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {
                if ("text/plain".equals(part.getMimeType())) {
                    try {
                        if (part.getBody() != null && part.getBody().getData() != null) {
                            byte[] decodedBytes = Base64.getUrlDecoder().decode(part.getBody().getData());
                            body = new String(decodedBytes);
                            break;
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Błąd dekodowania base64: " + e.getMessage());
                    }
                }
            }
        } else {
            System.err.println("Brak części wiadomości lub payloadu.");
        }
        return body;
    }

    //SEND EMAIL

    public void sendEmailFromFile(String recipent, String subject, String filePath) {

        String emailContent = null;
        try {
            emailContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            sendEmail(recipent, subject, emailContent);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEmail(String recipient, String subject, String emailContent) throws MessagingException, IOException {
        System.out.println("Sending email to : " + recipient);

        Gmail service = getGoogleService();

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("me"));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
        email.setSubject(subject);
        email.setText(emailContent);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);
        Message message = new Message().setRaw(encodedEmail);

        service.users().messages().send("me", message).execute();
        System.out.println("Email sent to: " + recipient);
    }
}
