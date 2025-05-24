package Controller;

import Services.UserService;
import dao.RefreshTokenDao;
import enums.Role;
import model.*;
import observers.ForgetPasswordObserver;
import observers.LoginObserver;
import observers.SignUpObserver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.JwtUtil;

import java.time.LocalDateTime;
import java.util.*;

import static Controller.UserFactory.createUser;


public class AuthController {

    private static AuthController instance;

    private final UserService userController = UserService.getInstance();
    private final List<LoginObserver> loginObservers = new ArrayList<>();
    private final List<SignUpObserver> signUpObservers = new ArrayList<>();
    private final List<ForgetPasswordObserver> forgetPasswordObservers = new ArrayList<>();
    private final Map<String, Long> resetTimestamps = new HashMap<>();

    private final RefreshTokenDao refreshTokenDao = new RefreshTokenDao();

    private AuthController() {
    }

    public static AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }

    public String login(String email, String password) {
        User user = userController.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("Invalid credentials.");
            return null;
        }

        String accessToken = JwtUtil.generateToken(user);
        String refreshTokenStr = JwtUtil.generateRefreshToken(user, 7 * 24 * 60 * 60 * 1000);

        RefreshToken refreshToken = new RefreshToken(refreshTokenStr, user, LocalDateTime.now().plusDays(7));
        refreshTokenDao.deleteByUser(user);
        refreshTokenDao.save(refreshToken);

        for (LoginObserver obs : loginObservers) {
            obs.onUserLoggedIn(user);
        }

        return accessToken;
    }

    public String generateRefreshToken(@NotNull User user, long refreshTokenValidityMs) {
        String refreshTokenStr = JwtUtil.generateRefreshToken(user, refreshTokenValidityMs);
        RefreshToken refreshToken = new RefreshToken(refreshTokenStr, user, LocalDateTime.now().plusNanos(refreshTokenValidityMs * 1_000_000L));
        refreshTokenDao.deleteByUser(user);
        refreshTokenDao.save(refreshToken);
        return refreshTokenStr;
    }

    public boolean isTokenExpired(String token) {
        return JwtUtil.isTokenExpired(token);
    }

    public User register(Role role, String fullName, String phoneNumber, String email, String password,
                         String address, Restaurant restaurant, String profileImageBase64,
                         String bankName, String accountNumber) {
        if (userController.findByEmail(email) != null) {
            System.out.println("Email already exists.");
            return null;
        }

        User user;
        try {
            user = createUser(role, fullName, phoneNumber, email, password, bankName, accountNumber, address, restaurant, profileImageBase64);
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
        resetTimestamps.put(email, System.currentTimeMillis());

        for (ForgetPasswordObserver obs : forgetPasswordObservers) {
            obs.onForgetPassword(user, code);
        }

        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                if (isCodeExpired(email)) {
                    System.out.println("Reset code expired. Please request a new one.");
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
        } finally {
            cleanupReset(email);
            scanner.close();
        }
    }

    public void deleteAccount(String token) {
        try {
            User user = requireLogin(token);
            refreshTokenDao.deleteByUser(user);
            boolean removed = userController.removeUser(user);
            System.out.println(removed ? "Account deleted." : "Failed to delete account.");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void editProfile(String token, String fullName, String phoneNumber, String email, String password,
                            String address, Location location) {
        User user = requireLogin(token);

        userController.updateBasicProfile(user, fullName, phoneNumber, email, password);

        switch (user.getRole()) {
            case CUSTOMER:
                if (user instanceof Customer customer) {
                    userController.updateCustomerDetails(customer, address, location);
                }
                break;
            case OWNER:
                if (user instanceof Owner owner) {
                    userController.updateOwnerDetails(owner, address, location);
                }
                break;
            case DELIVERY_MAN:
                if (user instanceof Deliveryman deliveryman) {
                    userController.updateDeliveryLocation(deliveryman, location);
                }
                break;
        }
        System.out.println("Profile updated for " + (user.getFullName() != null ? user.getFullName() : "user with ID " + user.getPublicId()));
    }

    @Contract("null -> fail")
    public @NotNull User requireLogin(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("No token provided.");
        }
        UserPayload payload = JwtUtil.verifyToken(token);
        if (payload == null) {
            throw new IllegalStateException("Invalid or expired token payload.");
        }
        User user = userController.findByPublicId(payload.getPublicId());
        if (user == null) {
            throw new IllegalStateException("User not found for token or token expired.");
        }
        return user;
    }

    private boolean isCodeExpired(String email) {
        Long sent = resetTimestamps.get(email);
        return sent == null || (System.currentTimeMillis() - sent) > 60_000;
    }

    private void cleanupReset(String email) {
        resetTimestamps.remove(email);
    }

    private int createResetCode() {
        return new Random().nextInt(10000, 100000);
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
}

class UserFactory {
    public static @NotNull User createUser(
            @NotNull Role role,
            String fullName,
            String phoneNumber,
            String email,
            String password,
            String bankName,
            String accountNumber,
            String address,
            Restaurant restaurant,
            String profileImageBase64
    ) {
        return switch (role) {
            case CUSTOMER -> new Customer(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
            case OWNER -> {
                if (restaurant == null) throw new IllegalArgumentException("Restaurant required for Owner");
                yield new Owner(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber, restaurant);
            }
            case DELIVERY_MAN ->
                    new Deliveryman(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}