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
import java.util.*;

public class MailService {

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.send"
    );

    private final Gmail service;

    public MailService(){
        this.service = getGoogleService();
    }

    public static Gmail getGoogleService() {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return new Gmail.Builder(HTTP_TRANSPORT, Utils.JSON_FACTORY, Utils.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("PLANNER APP")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> getRecentEmails(Long count) {
        List<String> output = new ArrayList<>();
        ListMessagesResponse messagesResponse;
        try {
            messagesResponse = service.users().messages().list("me")
                    .setMaxResults(count)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (messagesResponse.getMessages() == null || messagesResponse.getMessages().isEmpty()) {
            output.add("No messages.");
        } else {
            output.add("Last " + count + " messages:");
            for (Message message : messagesResponse.getMessages()) {
                Message fullMessage;
                try {
                    fullMessage = service.users().messages().get("me", message.getId()).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String subject = "No topic";
                for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                    if ("Subject".equalsIgnoreCase(header.getName())) {
                        subject = header.getValue();
                        break;
                    }
                }
                output.add("Subject: " + subject);
            }
        }
        return output;
    }

    public List<String> getEmailByIndex(int index){
        List<String> output = new ArrayList<>();
        long count = index+1;
        ListMessagesResponse messagesResponse;
        try {
            messagesResponse = service.users().messages().list("me")
                    .setMaxResults(count)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (messagesResponse.getMessages() == null || messagesResponse.getMessages().isEmpty()) {
            output.add("Brak wiadomości.");
        } else {
            if (index < 0 || index >= messagesResponse.getMessages().size()) {
                output.add("Indeks poza zakresem.");
            } else {
                Message message = messagesResponse.getMessages().get(index);
                Message fullMessage;
                try {
                    fullMessage = service.users().messages().get("me", message.getId()).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String subject = "Brak tematu";
                for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                    if ("Subject".equalsIgnoreCase(header.getName())) {
                        subject = header.getValue();
                        break;
                    }
                }
                output.add("Temat: " + subject);
                String body;
                try {
                    body = getBody(fullMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                output.add(body);
            }
        }
        return output;
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

    public String sendEmailFromFile(String recipient, String subject, String filePath) {
        String emailContent;
        System.out.println(filePath);
        try {
            emailContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException("Nie udało się odczytać pliku: " + e.getMessage(), e);
        }

        try {
            return sendEmail(recipient, subject, emailContent);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Nie udało się wysłać maila: " + e.getMessage(), e);
        }
    }

    public String sendEmail(String recipient, String subject, String emailContent) throws MessagingException, IOException {
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

        return "Email sent to: " + recipient;
    }
}
