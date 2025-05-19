package Services;

import controller.UserController;
import enums.Role;
import model.*;
import observers.ForgetPasswordObserver;
import observers.LoginObserver;
import observers.SignUpObserver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.JwtUtil;


import java.util.*;

public class AuthService {
    private static AuthService instance;

    private final UserController userController = UserController.getInstance();
    private final List<LoginObserver> loginObservers = new ArrayList<>();
    private final List<SignUpObserver> signUpObservers = new ArrayList<>();
    private final List<ForgetPasswordObserver> forgetPasswordObservers = new ArrayList<>();

    private final Map<String, Long> resetTimestamps = new HashMap<>();
    private final Map<String, Integer> resetCode = new HashMap<>();

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public String login(String email, String password) {
        User user = userController.findByEmail(email);
        if (user == null) {
            System.out.println("User not found.");
            return null;
        }
        if (!user.getPassword().equals(password)) {
            System.out.println("Wrong password.");
            return null;
        }

        String jwt = JwtUtil.generateToken(user);
        for (LoginObserver obs : loginObservers) {
            obs.onUserLoggedIn(user);
        }
        return jwt;
    }

    public String generateRefreshToken(@NotNull User user, long refreshTokenValidityMs) {
        return JwtUtil.generateRefreshToken(user, refreshTokenValidityMs);
    }

    public boolean isTokenExpired(String token) {
        return JwtUtil.isTokenExpired(token);
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
        if (userController.findByEmail(email) != null) {
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

        userController.addUser(user);
        for (SignUpObserver obs : signUpObservers) {
            obs.onUserRegistered(user);
        }
        return user;
    }

    public void requestPasswordReset(String email) {
        User user = userController.findByEmail(email);
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
                userController.resetPassword(user, newPass);
                System.out.println("Password reset successful.");
                break;
            } else {
                System.out.println("Incorrect code. Try again.");
            }
        }
        cleanupReset(email);
    }

    public void deleteAccount(String token) {
        try {
            User user = requireLogin(token);
            boolean removed = userController.removeUser(user);
            if (removed) {
                System.out.println("Account for " + user.getFirstName() + " has been deleted.");
            } else {
                System.out.println("Error: Failed to delete account for " + user.getFirstName() + ".");
            }
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void editProfile(
            String token,
            String firstName,
            String lastName,
            String phone,
            String email,
            String password,
            Address address,
            Location location
    ) {
        User user = requireLogin(token);
        userController.updateBasicProfile(user, firstName, lastName, phone, email, password);
        switch (user.getRole()) {
            case CUSTOMER -> userController.updateCustomerDetails(
                    (Customer) user, address, location);
            case OWNER -> userController.updateOwnerDetails(
                    (Owner) user, address, location);
            case DELIVERY_MAN -> userController.updateDeliveryLocation(
                    (Deliveryman) user, location);
        }
        System.out.println("Profile updated for " + user.getFirstName());
    }

    @Contract("null -> fail")
    public @NotNull User requireLogin(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("No token provided.");
        }
        UserPayload payload = JwtUtil.verifyToken(token);
        User user = userController.findByPublicId(payload.getPublicId());
        if (user == null) {
            throw new IllegalStateException("Invalid or expired token.");
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

    public void registerLoginObserver(LoginObserver obs) {
        loginObservers.add(obs);
    }

    public void registerSignUpObserver(SignUpObserver obs) {
        signUpObservers.add(obs);
    }

    public void registerForgetPasswordObserver(ForgetPasswordObserver obs) {
        forgetPasswordObservers.add(obs);
    }

    public UserController getUserManager() {
        return userController;
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
