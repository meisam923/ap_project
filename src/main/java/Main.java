import exception.NotAcceptableException;
import model.*;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        AuthService authService = AuthService.getInstance();
        NotificationService notificationService = new NotificationService();

        //SignUpObserver
        authService.registerSignUpObserver(notificationService);
        //LoginObserver
        authService.registerLoginObserver(notificationService);
        //ForgetPasswordObserver
        authService.registerForgetPasswordObserver(notificationService);

        // RegisterCUSTOMER
        User customer = authService.register(
                Role.CUSTOMER,
                "Ali",
                "Rezai",
                "09121234567",
                "ali@gmail.com",
                "my_password",
                new Location(35.7, 51.4),
                new Address("Tehran12345", "Home"),
                null  // no restaurant for customers
        );

        // RegisterOWNER
        Restaurant sampleRestaurant = new Restaurant();
        User owner = authService.register(
                Role.OWNER,
                "Sara",
                "Ahmadi",
                "09351234567",
                "seyedmeysamhamidi7@gmail.com", // my email for test
                "owner_pass",
                new Location(35.8, 51.5),
                new Address("Street", "54321"),
                sampleRestaurant   // made a no args restaurant constructor for testing
        );

        // print test :) -> seems fine
        System.out.println("\nAll Registered Users:");
        for (User u : UserManager.getInstance().getAllUsers()) {
            System.out.println("- " + u.getRole() + ": " + u.getFirstName() + " " + u.getLastName() + " (" + u.getEmail() + ")");
        }

        authService.login("rezaj123rezaj123@gmail.com", "owner_pass");
        //authService.requestPasswordReset("rezaj123rezaj123@gmail.com");
        authService.deleteAccount(owner.getSessionToken());
        authService.requestPasswordReset("rezaj123rezaj123@gmail.com");
        authService.logOut(owner.getSessionToken());
        authService.deleteAccount(owner.getSessionToken());

        RestaurantManager restaurantManager = RestaurantManager.getInstance();
        restaurantManager.addRestaurantObserver(notificationService);
        try {
            restaurantManager.addRestaurant(new Address("ksj","slkjf"),new Location(89,99),"09877654321","pizzaiii",(Owner)owner,new ArrayList<Period>());
        } catch (NotAcceptableException e) {
            throw new RuntimeException(e);
        }
    }
}
