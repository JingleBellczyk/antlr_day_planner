package logic;

import com.google.api.services.gmail.model.Message;
import services.MailService;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class UserMailOperations {

    MailService mailService;

    public UserMailOperations() {
        this.mailService = new MailService();
    }

    public List<Message> getLastEmails(Long number) {
        System.out.println("Pobrano maile");
        List<Message> messages = mailService.getRecentEmails(number);
        return messages;
    }

    public void showEmailOnIndex(int index) {

        mailService.getEmailByIndex(index);
    }

    public void sendEmailFromFile(String recipient, String subject, String filePath) {
        mailService.sendEmailFromFile(recipient, subject, filePath);
    }

    public void sendEmail(String recipient, String subject, String emailContent) {
        try {
            mailService.sendEmail(recipient, subject, emailContent);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
