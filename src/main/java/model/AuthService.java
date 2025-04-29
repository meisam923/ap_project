package model;

import java.util.ArrayList;
import java.util.List;

//singleton class
public class AuthService {
    private static AuthService instance;
    private final UserManager userManager = UserManager.getInstance();
    private final List<SignUpObserver> observers = new ArrayList<>();

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

        return user; //login Successful
    }

    public void registerObserver(SignUpObserver observer) {
        observers.add(observer);
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
        for (SignUpObserver observer : observers) {
            observer.onUserRegistered(user);
        }

        return user;
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
