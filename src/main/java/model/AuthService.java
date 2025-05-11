package model;

import java.util.*;


public class AuthService {
    private static AuthService instance;

    private final UserManager userManager = UserManager.getInstance();
    private final List<LoginObserver> loginObservers = new ArrayList<>();
    private final List<SignUpObserver> signUpObservers = new ArrayList<>();
    private final List<ForgetPasswordObserver> forgetPasswordObservers = new ArrayList<>();

    private final Map<String, Long> resetTimestamps = new HashMap<>();
    private final Map<String, Integer> resetCode = new HashMap<>();

    private AuthService() { }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }


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

        // Create session
        String token = SessionManager.login(user);
        user.setSessionToken(token);

        // Notify observers
        for (LoginObserver observer : loginObservers) {
            observer.onUserLoggedIn(user);
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
        for (SignUpObserver observer : signUpObservers) {
            observer.onUserRegistered(user);
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

        // Console prompt loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (isCodeExpired(email)) {
                System.out.println("Reset code expired. Please request a new one.");
                cleanupReset(email);
                return;
            }

            System.out.print("Enter the reset code (you have 1 minute): ");
            String input = scanner.nextLine().trim();
            int enteredCode;
            try {
                enteredCode = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid format. Please enter digits only.");
                continue;
            }

            if (enteredCode == code) {
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine();
                userManager.resetPassword(user, newPassword);
                System.out.println("Password reset successful.");
                break;
            } else {
                System.out.println("Incorrect code. Try again.");
            }
        }

        cleanupReset(email);
    }


    public void logOut(User user) {
        if (user == null) {
            System.out.println("No user provided to log out.");
            return;
        }
        String token = user.getSessionToken();
        if (token == null || !SessionManager.isLoggedIn(user)) {
            System.out.println("User is not currently logged in.");
            return;
        }
        SessionManager.logout(token);
        user.setSessionToken(null);
        System.out.println("Logged out successfully.");
    }


    public void deleteAccount(String token) {
        if (token == null || token.isBlank()) {
            System.out.println("No session token provided.");
            return;
        }
        User user = SessionManager.getUserByToken(token);
        if (user == null) {
            System.out.println("Invalid or expired session token.");
            return;
        }


        SessionManager.logout(token);
        user.setSessionToken(null);


        boolean removed = userManager.removeUser(user);

        if (removed) {
            System.out.println("Account for " + user.getFirstName() + " has been deleted.");
        } else {
            System.out.println("Error: Failed to delete account for " + user.getFirstName() + ".");
        }
    }




    private boolean isCodeExpired(String email) {
        Long sentTime = resetTimestamps.get(email);
        return sentTime == null || (System.currentTimeMillis() - sentTime) > 60_000;
    }

    private void cleanupReset(String email) {
        resetCode.remove(email);
        resetTimestamps.remove(email);
    }

    private int createResetCode() {
        return new Random().nextInt(10000, 99999);
    }

    // Observer registration
    public void registerLoginObserver(LoginObserver obs)       { loginObservers.add(obs); }
    public void registerSignUpObserver(SignUpObserver obs)      { signUpObservers.add(obs); }
    public void registerForgetPasswordObserver(ForgetPasswordObserver obs) {
        forgetPasswordObservers.add(obs);
    }

    // Accessors
    public UserManager getUserManager()                         { return userManager; }
    public List<LoginObserver> getLoginObservers()              { return loginObservers; }
    public List<SignUpObserver> getSignUpObservers()            { return signUpObservers; }
    public Map<String, Integer> getResetCode()                  { return Collections.unmodifiableMap(resetCode); }
}


class UserFactory {
    public static User createUser(
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
        return switch (role) {
            case CUSTOMER -> new Customer(firstName, lastName, phone, email, password, address, location);
            case OWNER -> {
                if (restaurant == null) throw new IllegalArgumentException("Restaurant required for Owner");
                yield new Owner(firstName, lastName, phone, email, password, address, location, restaurant);
            }
            case DELIVERY_MAN -> new Deliveryman(firstName, lastName, phone, email, password, location);
            default -> throw new IllegalArgumentException("Invalid role");
        };
    }
}
