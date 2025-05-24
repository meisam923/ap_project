package Controller;

import dao.CustomerDao;
import dao.DeliverymanDao;
import dao.OwnerDao;
import lombok.Getter;
import lombok.Setter;
import model.*; // Assuming this imports User, Customer, Owner, Deliveryman, Location

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserController {
    private static UserController instance;

    private final CustomerDao customerDao = new CustomerDao();
    private final OwnerDao ownerDao = new OwnerDao();
    private final DeliverymanDao deliverymanDao = new DeliverymanDao();

    private UserController() {
    }

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public void addUser(User user) {
        if (user == null) return;

        switch (user) {
            case Customer customer -> customerDao.save(customer);
            case Owner owner -> ownerDao.save(owner);
            case Deliveryman deliveryman -> deliverymanDao.save(deliveryman);
            default -> System.out.println("Unknown user type, cannot add: " + user.getClass().getName());
        }
    }

    public User findByPublicId(String publicId) {
        User user;
        user = customerDao.findByPublicId(publicId);
        if (user != null) return user;
        user = ownerDao.findByPublicId(publicId);
        if (user != null) return user;
        user = deliverymanDao.findByPublicId(publicId);
        if (user != null) return user;
        return null;
    }

    public User findByEmail(String email) {
        User user;
        user = customerDao.findByEmail(email);
        if (user != null) return user;
        user = ownerDao.findByEmail(email);
        if (user != null) return user;
        user = deliverymanDao.findByEmail(email);
        if (user != null) return user;
        return null;
    }

    public void resetPassword(User user, String password) {
        if (user == null || password == null || password.isBlank()) return;
        user.setPassword(password);

        switch (user) {
            case Customer customer -> customerDao.update(customer);
            case Owner owner -> ownerDao.update(owner);
            case Deliveryman deliveryman -> deliverymanDao.update(deliveryman);
            default -> System.out.println("Unknown user type, cannot reset password: " + user.getClass().getName());
        }
    }

    public boolean removeUser(User user) {
        if (user == null) return false;

        switch (user) {
            case Customer customer -> customerDao.delete(customer);
            case Owner owner -> ownerDao.delete(owner);
            case Deliveryman deliveryman -> deliverymanDao.delete(deliveryman);
            default -> {
                System.out.println("Unknown user type, cannot remove: " + user.getClass().getName());
                return false;
            }
        }
        return true;
    }

    public boolean updateBasicProfile(User user, String fullName, String phoneNumber, String email, String password) {
        if (user == null) return false;

        boolean changed = false;
        if (fullName != null && !fullName.equals(user.getFullName())) {
            user.setFullName(fullName);
            changed = true;
        }
        if (phoneNumber != null && !phoneNumber.equals(user.getPhoneNumber())) {
            user.setPhoneNumber(phoneNumber);
            changed = true;
        }
        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            changed = true;
        }
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
            changed = true;
        }

        if (changed) {
            switch (user) {
                case Customer customer -> customerDao.update(customer);
                case Owner owner -> ownerDao.update(owner);
                case Deliveryman deliveryman -> deliverymanDao.update(deliveryman);
                default -> {
                    System.out.println("Unknown user type, basic profile not updated in DB: " + user.getClass().getName());
                    return false;
                }
            }
        }
        return changed;
    }

    public boolean updateCustomerDetails(Customer customer, String newAddress, Location newLocation) {
        if (customer == null) return false;
        boolean changed = false;

        if (newAddress != null && !newAddress.equals(customer.getAddress())) {
            customer.setAddress(newAddress);
            changed = true;
        }
        if (newLocation != null && (customer.getLocation() == null || !newLocation.equals(customer.getLocation()))) {
            customer.setLocation(newLocation);
            changed = true;
        }

        if (changed) {
            customerDao.update(customer);
        }
        return changed;
    }

    public boolean updateOwnerDetails(Owner owner, String newAddress, Location newLocation) {
        if (owner == null) return false;
        boolean changed = false;

        if (newAddress != null && !newAddress.equals(owner.getAddress())) {
            owner.setAddress(newAddress);
            changed = true;
        }
        if (newLocation != null && (owner.getLocation() == null || !newLocation.equals(owner.getLocation()))) {
            owner.setLocation(newLocation);
            changed = true;
        }

        if (changed) {
            ownerDao.update(owner);
        }
        return changed;
    }

    public boolean updateDeliveryLocation(Deliveryman dm, Location newLocation) {
        if (dm == null) return false;
        boolean changed = false;

        if (newLocation != null && (dm.getLocation() == null || !newLocation.equals(dm.getLocation()))) {
            dm.setLocation(newLocation); // Make sure Deliveryman class has setLocation(Location)
            changed = true;
        }

        if (changed) {
            deliverymanDao.update(dm);
        }
        return changed;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(customerDao.getAll());
        allUsers.addAll(ownerDao.getAll());
        allUsers.addAll(deliverymanDao.getAll());
        return allUsers;
    }
}