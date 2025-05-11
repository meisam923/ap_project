package model;

import jakarta.mail.MessagingException;

import java.util.*;

//singleton class
public class AuthService {
    private static AuthService instance;
    private final UserManager userManager = UserManager.getInstance();
    private final List<LoginObserver> loginObservers = new ArrayList<>();
    private final List<SignUpObserver> signUpObservers = new ArrayList<>();
    private final List<ForgetPasswordObserver> forgetPasswordObservers = new ArrayList<>();
    private final Map<String, Long> resetTimestamps = new HashMap<>();
    private final Map<String, Integer> resetCode = new HashMap<>();

    private AuthService() {
    }
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // Try to log in using email + password
    public User login(String email, String password) {
        User user = userManager.findByEmail(email);
        if (user == null) {
            System.out.println("User not found.");
            return null;
        }

        if (!user.getPassword().equals(password)) {
            System.out.println("Wrong password.");
            return null;
        }
        for (LoginObserver observer : loginObservers) {
            observer.onUserLoggedIn(user);
        }

        return user; //login Successful
    }

    public void registerSignInObserver(SignUpObserver observer) {
        signUpObservers.add(observer);
    }
    public void registerLoginObserver(LoginObserver observer) {
        loginObservers.add(observer);
    }
    public void registerForgetPasswordObserver(ForgetPasswordObserver observer) {
        forgetPasswordObservers.add(observer);
    }


    public User register(Role role,
                         String firstName,
                         String lastName,
                         String phone,
                         String email,
                         String password,
                         Location location,
                         Address address,
                         Restaurant restaurant  // only needed for OWNER
    ) {

        if (userManager.findByEmail(email) != null) {
            System.out.println("Email already exists.");
            return null;
        }

        // Create the user using the factory
        User user;
        try {
            user = UserFactory.createUser(role, firstName, lastName, phone, email, password, location, address, restaurant);
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }

        // Save the user
        userManager.addUser(user);

        // Notify observers
        for (SignUpObserver observer : signUpObservers) {
            observer.onUserRegistered(user);
        }

        return user;
    }
    // when user forgets their password
    public void requestPasswordReset(String email) {
        User user = userManager.findByEmail(email);
        if (user == null) {
            System.out.println("Email not found.");
            return;
        }
        int randomCode = createResetCode();
        resetCode.put(user.getEmail(), randomCode);
        resetTimestamps.put(user.getEmail(), System.currentTimeMillis());

        // Send the reset code via email
        try {
            String subject = "Password Reset Code";
            String body = "Here is your reset code: " + randomCode + ". Please use this to reset your password.";
            EmailService.sendEmail(user.getEmail(), subject, body);
            System.out.println("Reset code sent to your email.");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email.");
            return;
        }

        // Notify all registered ForgetPasswordObservers
        for (ForgetPasswordObserver obs : forgetPasswordObservers) {
            obs.onForgetPassword(user, randomCode);
        }

        // Wait for user input
        Scanner scanner = new Scanner(System.in);// TODO: this must change later
        System.out.print("Enter the reset code: ");
        int codeEntered = Integer.parseInt(scanner.nextLine());
        Integer expected = resetCode.get(email);
        //set timer for password
        Long sentTime = resetTimestamps.get(email);
        long currentTime = System.currentTimeMillis();
        if (sentTime == null || (currentTime - sentTime) > 60_000) {
            System.out.println("Reset code expired. Please request a new one.");
            resetCode.remove(email);
            resetTimestamps.remove(email);
            return;
        }

        if (expected != null && expected == codeEntered) {
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine();
            userManager.resetPassword(user, newPassword);
            resetCode.remove(email);
            resetTimestamps.remove(email);
            System.out.println("Password reset successful.");
        } else {
            System.out.println("Incorrect code. Password reset failed.");
        }
    }


    private int createResetCode() {
        Random random = new Random();
        return random.nextInt(10000,99999);
    }

    public UserManager getUserManager() {
        return userManager;
    }
    public List<LoginObserver> getLoginObservers() {
        return loginObservers;
    }
    public List<SignUpObserver> getSignUpObservers() {
        return signUpObservers;
    }
    public Map<String, Integer> getResetCode() {
        return resetCode;
    }
}


class UserFactory {  //only used inside this package no direct access

    public static User createUser(Role role,
                                  String firstName,
                                  String lastName,
                                  String phone,
                                  String email,
                                  String password,
                                  Location location,
                                  Address address,
                                  Restaurant restaurant // only needed for Owner
    ) {
        return switch (role) {
            case CUSTOMER -> new Customer(firstName, lastName, phone, email, password, address, location);
            case OWNER -> {
                if (restaurant == null) {
                    throw new IllegalArgumentException("Restaurant required for Owner");
                }
                yield new Owner(firstName, lastName, phone, email, password, address, location, restaurant);
            }
            case DELIVERY_MAN -> new Deliveryman(firstName, lastName, phone, email, password, location);
            default -> {
                throw new IllegalArgumentException("Invalid role"); //admin doesnt extend user so cant be used for admin
            }
        };
    }


}
