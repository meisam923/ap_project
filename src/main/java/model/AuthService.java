package model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;


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

    //methods
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

        String token = SessionManager.login(user);
        user.setSessionToken(token);

        // notify observers
        for (LoginObserver obs : loginObservers) {
            obs.onUserLoggedIn(user);
        }
        return user;
    }

    public User register(
            Role role,
            String firstName,
            String lastName,
            String phone,
            String email,
            String password,
            Location location,
            Address address,
            Restaurant restaurant
    ) {
        if (userManager.findByEmail(email) != null) {
            System.out.println("Email already exists.");
            return null;
        }
        User user;
        try {
            user = UserFactory.createUser(
                    role, firstName, lastName,
                    phone, email, password,
                    location, address, restaurant
            );
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }

        userManager.addUser(user);
        for (SignUpObserver obs : signUpObservers) {
            obs.onUserRegistered(user);
        }
        return user;
    }

    public void requestPasswordReset(String email) {
        User user = userManager.findByEmail(email);
        if (user == null) {
            System.out.println("Email not found.");
            return;
        }

        int code = createResetCode();
        resetCode.put(email, code);
        resetTimestamps.put(email, System.currentTimeMillis());

        for (ForgetPasswordObserver obs : forgetPasswordObservers) {
            obs.onForgetPassword(user, code);
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (isCodeExpired(email)) {
                System.out.println("Reset code expired. Please request a new one.");
                cleanupReset(email);
                return;
            }

            System.out.print("Enter the reset code (you have 1 minute): ");
            String input = scanner.nextLine().trim();
            int entered;
            try {
                entered = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid format. Please enter digits only.");
                continue;
            }

            if (entered == code) {
                System.out.print("Enter new password: ");
                String newPass = scanner.nextLine();
                userManager.resetPassword(user, newPass);
                System.out.println("Password reset successful.");
                break;
            } else {
                System.out.println("Incorrect code. Try again.");
            }
        }
        cleanupReset(email);
    }

    public void logOut(String sessionToken) {
        try {
            User user = requireLogin(sessionToken);
            SessionManager.logout(sessionToken);
            user.setSessionToken(null);
            System.out.println("Logged out successfully for " + user.getFirstName() + ".");
        } catch (IllegalStateException e) {
            // Gracefully handle missing/invalid token
            System.out.println(e.getMessage());
        }
    }

    public void deleteAccount(String sessionToken) {
        try {
            User user = requireLogin(sessionToken);
            SessionManager.logout(sessionToken);
            user.setSessionToken(null);

            boolean removed = userManager.removeUser(user);
            if (removed) {
                System.out.println("Account for " + user.getFirstName() + " has been deleted.");
            } else {
                System.out.println("Error: Failed to delete account for " + user.getFirstName() + ".");
            }
        } catch (IllegalStateException e) {
            // Gracefully handle missing/invalid token
            System.out.println(e.getMessage());
        }
    }

    public void editProfile(
            String sessionToken,
            String firstName,
            String lastName,
            String phone,
            String email,
            String password,
            Address address,     // may be null
            Location location    // may be null
    ) {
        User user = requireLogin(sessionToken);

        userManager.updateBasicProfile(user, firstName, lastName, phone, email, password);

        switch (user.getRole()) {
            case CUSTOMER -> userManager.updateCustomerDetails(
                    (Customer) user, address, location);
            case OWNER -> userManager.updateOwnerDetails(
                    (Owner) user, address, location);
            case DELIVERY_MAN -> userManager.updateDeliveryLocation(
                    (Deliveryman) user, location);
        }
        System.out.println("Profile updated for " + user.getFirstName());
    }


    //helper methods
    @Contract("null -> fail") // intellij suggested this
    public @NotNull User requireLogin(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("No session token provided.");
        }
        User user = SessionManager.getUserByToken(token);
        if (user == null) {
            throw new IllegalStateException("Invalid or expired session token.");
        }
        return user;
    }

    private boolean isCodeExpired(String email) {
        Long sent = resetTimestamps.get(email);
        return sent == null || (System.currentTimeMillis() - sent) > 60_000;
    }

    private void cleanupReset(String email) {
        resetCode.remove(email);
        resetTimestamps.remove(email);
    }

    private int createResetCode() {
        return new Random().nextInt(10000, 99999);
    }

    //observers
    public void registerLoginObserver(LoginObserver obs) {
        loginObservers.add(obs);
    }

    public void registerSignUpObserver(SignUpObserver obs) {
        signUpObservers.add(obs);
    }

    public void registerForgetPasswordObserver(ForgetPasswordObserver obs) {
        forgetPasswordObservers.add(obs);
    }

    //getters
    public UserManager getUserManager() {
        return userManager;
    }

    public List<LoginObserver> getLoginObservers() {
        return List.copyOf(loginObservers);
    }

    public List<SignUpObserver> getSignUpObservers() {
        return List.copyOf(signUpObservers);
    }

    public Map<String, Integer> getResetCode() {
        return Collections.unmodifiableMap(resetCode);
    }
}


class UserFactory {
    public static @NotNull User createUser(
            @NotNull Role role,
            String firstName,
            String lastName,
            String phone,
            String email,
            String password,
            Location location,
            Address address,
            Restaurant restaurant
    ) {
        return switch (role) {
            case CUSTOMER -> new Customer(firstName, lastName, phone, email, password, address, location);
            case OWNER -> {
                if (restaurant == null) throw new IllegalArgumentException("Restaurant required for Owner");
                yield new Owner(firstName, lastName, phone, email, password, address, location);
            }
            case DELIVERY_MAN -> new Deliveryman(firstName, lastName, phone, email, password, location);
            default -> throw new IllegalArgumentException("Invalid role");
        };
    }
}
