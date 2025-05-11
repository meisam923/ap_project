import model.*;

public class Main {
    public static void main(String[] args) {
        AuthService authService = AuthService.getInstance();
        NotificationService notificationService = new NotificationService();

        //SignUpObserver
        authService.registerSignInObserver(notificationService);
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
                "mypassword",
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
                "rezaj123rezaj123@gmail.com",
                "ownerpass",
                new Location(35.8, 51.5),
                new Address("Street", "54321"),
                sampleRestaurant   // made a no args restaurant constructor for testing
        );

        // print test :) -> seems fine
        System.out.println("\nAll Registered Users:");
        for (User u : UserManager.getInstance().getAllUsers()) {
            System.out.println("- " + u.getRole() + ": " + u.getFirstName() + " " + u.getLastName() + " (" + u.getEmail() + ")");
        }

        authService.login("rezaj123rezaj123@gmail.com",
                "ownerpass");
        authService.requestPasswordReset("rezaj123rezaj123@gmail.com");
    }
}
