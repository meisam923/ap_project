package model;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";  // Use Gmail's SMTP server, or another provider
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "rezamh823@gmail.com";
    private static final String PASSWORD = System.getenv("EMAIL_APP_PASSWORD");


    public static void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body, "utf-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent("<h2>" + subject + "</h2><p>" + body + "</p>", "text/html");

        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);

        message.setContent(multipart);

        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}

