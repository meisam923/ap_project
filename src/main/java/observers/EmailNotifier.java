package observers;

import model.User;
import Services.EmailService;

public class EmailNotifier implements SignUpObserver, ForgetPasswordObserver {

    @Override
    public void onUserRegistered(User user) {
        try {
            String subject = "Welcome to Poly Eats!";
            String body = "Hi " + user.getFullName() + ",\n\nThank you for registering with Poly Eats. We're excited to have you!";
            EmailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("Registration email sent to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send registration email to " + user.getEmail());
            e.printStackTrace();
        }
    }

    @Override
    public void onForgetPassword(User user, int resetCode) {
        try {
            String subject = "Your Poly Eats Password Reset Code";
            String body = "Hi " + user.getFullName() + ",\n\nYour password reset code is: " + resetCode + "\n\nThis code will expire in 5 minutes.";
            EmailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("Password reset email sent to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to " + user.getEmail());
            e.printStackTrace();
        }
    }
}