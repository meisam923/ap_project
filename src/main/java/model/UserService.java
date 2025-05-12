package model;

public class UserService {
    private final UserManager userManager = UserManager.getInstance();

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

        userManager.updateBasicProfile(
                user, firstName, lastName, phone, email, password);

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
}
