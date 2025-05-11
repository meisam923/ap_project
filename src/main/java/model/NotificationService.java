package model;

import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;

public class NotificationService implements SignUpObserver, LoginObserver, ForgetPasswordObserver {
    AuthService authService = AuthService.getInstance();
    @Override
    public void onUserRegistered(@NotNull User user) {
        System.out.println("Welcome " + user.getFirstName() + " Notification sent");
        try {
            EmailService.sendEmail(user.getEmail(), "Welcome!", "Thanks for signing up, " + user.getFirstName() + "!");
        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email.");
            e.printStackTrace();
        }
    }

    @Override
    public void onUserLoggedIn(@NotNull User user) {
        System.out.println("Welcome Back " + user.getFirstName() + " Notification sent");
    }
    @Override
    public void onForgetPassword(@NotNull User user, int resetCode) {
        System.out.println("A password reset was requested for " + user.getFirstName() +
                ". Reset code sent to " + user.getEmail() + ": ");
    }
}

