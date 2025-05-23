package Services;

import dao.RefreshTokenDao;
import jakarta.mail.MessagingException;
import model.Restaurant;
import model.User;
import observers.ForgetPasswordObserver;
import observers.LoginObserver;
import observers.RestaurantObserver;
import observers.SignUpObserver;
import org.jetbrains.annotations.NotNull;


public class NotificationService implements SignUpObserver, LoginObserver, ForgetPasswordObserver, RestaurantObserver {

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
    public void registerRestaurant(@NotNull Restaurant restaurant) {
        System.out.println("Welcome " + restaurant.getOwner().getFirstName() + "!restaurant request has been sent");
        String subject = "Congratulation ! your restaurant " + restaurant.getTitle() + " will be registered ASAP"  + restaurant.getOwner().getFirstName() + "!";
        String body = "Welcome to your new journey" + restaurant.getOwner().getFirstName() + ",\n\n"
                + "We're glad to have you in PolyEats. Enjoy using PolyEats!"
                + "\n\nBest regards,\nThe PolyEats Team";
        try {
            EmailService.sendEmail(restaurant.getOwner().getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Failed to send login notification email.");
            e.printStackTrace();
        }

    }
}

