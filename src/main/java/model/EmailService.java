package model;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;


public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
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
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body, "utf-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        String htmlContent = """
                <div style="max-width:600px;margin:0 auto;font-family:Arial,sans-serif;color:#333;">
                  <div style="background:#f7f7f7;padding:20px 30px;text-align:center;border-bottom:1px solid #ddd;">
                    <h1 style="margin:0;font-size:24px;color:#4a90e2;">%s</h1>
                  </div>
                  <div style="padding:30px;">
                    <p style="font-size:16px;line-height:1.5;">%s</p>
                  </div>
                  <div style="background:#f7f7f7;padding:15px 30px;text-align:center;border-top:1px solid #ddd;font-size:12px;color:#777;">
                    <p style="margin:0;">&copy; 2025 PolyEats, Inc. All rights reserved.</p>
                  </div>
                </div>
                """.formatted(subject, body);
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);

        message.setContent(multipart);

        Transport.send(message);
    }
}
