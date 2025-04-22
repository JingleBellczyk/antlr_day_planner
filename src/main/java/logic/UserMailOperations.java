package logic;

import services.MailService;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public class UserMailOperations {

    MailService mailService;

    public UserMailOperations() {
        this.mailService = new MailService();
    }

    public List<String> getLastEmails(Long number) {
        return mailService.getRecentEmails(number);
    }

    public List<String> showEmailOnIndex(int index) {
        return mailService.getEmailByIndex(index);
    }

    public String sendEmailFromFile(String recipient, String subject, String filePath) {
        return mailService.sendEmailFromFile(recipient, subject, filePath);
    }

    public String sendEmail(String recipient, String subject, String emailContent) {
        try {
            return mailService.sendEmail(recipient, subject, emailContent);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}