package model;

import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;


public class NotificationService implements SignUpObserver, LoginObserver, ForgetPasswordObserver ,RestaurantObserver{
    private final AuthService authService = AuthService.getInstance();

    @Override
    public void onUserRegistered(@NotNull User user) {
        System.out.println("Welcome " + user.getFirstName() + "! Sending welcome email...");
        String subject = "Welcome to PolyEats, " + user.getFirstName() + "!";
        String body = "Hi " + user.getFirstName() + ",\n\n"
                + "Thank you for registering with PolyEats. We're thrilled to have you!"
                + "\n\nBest regards,\nThe PolyEats Team";
        try {
            EmailService.sendEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email.");
            e.printStackTrace();
        }
    }

    @Override
    public void onUserLoggedIn(@NotNull User user) {
        System.out.println("Welcome back " + user.getFirstName() + "! Sending login notification...");
        String subject = "Welcome Back, " + user.getFirstName() + "!";
        String body = "Hi " + user.getFirstName() + ",\n\n"
                + "We're glad to see you again. Enjoy using PolyEats!"
                + "\n\nBest regards,\nThe PolyEats Team";
        try {
            EmailService.sendEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Failed to send login notification email.");
            e.printStackTrace();
        }
    }

    @Override
    public void onForgetPassword(@NotNull User user, int resetCode) {
        System.out.println("Password reset requested for " + user.getFirstName() + ". Sending reset code...");
        String subject = "PolyEats Password Reset Code";
        String body = "Hello " + user.getFirstName() + ",\n\n"
                + "Your password reset code is: " + resetCode + "\n"
                + "This code will expire in 1 minute.\n\n"
                + "If you did not request a password reset, please ignore this email."
                + "\n\nBest regards,\nThe PolyEats Team";
        try {
            EmailService.sendEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email.");
            e.printStackTrace();
        }
    }
    @Override
    public boolean registerRestaurant(@NotNull Restaurant restaurant,@NotNull Owner owner) {
        System.out.println("Welcome " + owner.getFirstName() + "!restaurant has been registered");
        String subject = "Congratulation ! your restaurant " + restaurant.getTitle() + " has been registered" + owner.getFirstName() + "!";
        String body = "Welcome to your new journey" + owner.getFirstName() + ",\n\n"
                + "We're glad to have you in PolyEats. Enjoy using PolyEats!"
                + "\n\nBest regards,\nThe PolyEats Team";
        try {
            EmailService.sendEmail(owner.getEmail(), subject, body);
            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send login notification email.");
            e.printStackTrace();
            return false;
        }
    }
}
