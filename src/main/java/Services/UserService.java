package Services;

import Controller.UserController;
import model.*;

public class UserService {
    private final UserController userController = UserController.getInstance();

    public void editProfile(
            String sessionToken,
            String firstName,
            String lastName,
            String phone,
            String email,
            String password,
            Address address,     // nullable
            Location location    // nullable
    ) {
        User user = AuthService.getInstance().requireLogin(sessionToken);

        userController.updateBasicProfile(user, firstName, lastName, phone, email, password);

        switch (user.getRole()) {
            case CUSTOMER -> {
                if (address == null || location == null) {
                    throw new IllegalArgumentException("Address and location must be provided for customer profile update.");
                }
                userController.updateCustomerDetails((Customer) user, address, location);
            }
            case OWNER -> {
                if (address == null || location == null) {
                    throw new IllegalArgumentException("Address and location must be provided for owner profile update.");
                }
                userController.updateOwnerDetails((Owner) user, address, location);
            }
            case DELIVERY_MAN -> {
                if (location == null) {
                    throw new IllegalArgumentException("Location must be provided for deliveryman profile update.");
                }
                userController.updateDeliveryLocation((Deliveryman) user, location);
            }
        }

        System.out.println("Profile updated for " + user.getFirstName());
    }
}
