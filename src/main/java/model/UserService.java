package model;

import Services.AuthService;
import controller.UserController;

public class UserService {
    private final UserController userController = UserController.getInstance();

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
        User user = AuthService.getInstance().requireLogin(sessionToken);

        userController.updateBasicProfile(
                user, firstName, lastName, phone, email, password);

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
}
