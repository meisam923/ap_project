package Controller;

import dao.CustomerDao;
import dao.DeliverymanDao;
import dao.OwnerDao;
import lombok.Getter;
import lombok.Setter;
import model.*;

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
            default -> System.out.println("Unknown user type");
        }
    }

    public User findByPublicId(String publicId) {
        User user;
        user = customerDao.findByPublicId(publicId);
        if (user != null) return user;
        user = ownerDao.findByPublicId(publicId);
        if (user != null) return user;
        return deliverymanDao.findByPublicId(publicId);
    }

    public User findByEmail(String email) {
        User user;
        user = customerDao.findByEmail(email);
        if (user != null) return user;
        user = ownerDao.findByEmail(email);
        if (user != null) return user;
        return deliverymanDao.findByEmail(email);
    }

    public void resetPassword(User user, String password) {
        if (user == null || password == null) return;
        user.setPassword(password);

        switch (user) {
            case Customer customer -> customerDao.update(customer);
            case Owner owner -> ownerDao.update(owner);
            case Deliveryman deliveryman -> deliverymanDao.update(deliveryman);
            default -> System.out.println("Unknown user type");
        }
    }

    public boolean removeUser(User user) {
        if (user == null) return false;

        switch (user) {
            case Customer customer -> customerDao.delete(customer);
            case Owner owner -> ownerDao.delete(owner);
            case Deliveryman deliveryman -> deliverymanDao.delete(deliveryman);
            default -> {
                System.out.println("Unknown user type");
                return false;
            }
        }

        return true;
    }

    public boolean updateBasicProfile(User user, String firstName, String lastName, String phone, String email, String password) {
        if (user == null) return false;

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phone != null) user.setPhoneNumber(phone);
        if (email != null) user.setEmail(email);
        if (password != null) user.setPassword(password);

        switch (user) {
            case Customer customer -> customerDao.update(customer);
            case Owner owner -> ownerDao.update(owner);
            case Deliveryman deliveryman -> deliverymanDao.update(deliveryman);
            default -> {
                System.out.println("Unknown user type");
                return false;
            }
        }

        return true;
    }

    public boolean updateCustomerDetails(Customer customer, Address newAddress, Location newLocation) {
        if (customer == null) return false;
        if (newAddress != null) customer.setAddress(newAddress);
        if (newLocation != null) customer.setLocation(newLocation);
        customerDao.update(customer);
        return true;
    }

    public boolean updateOwnerDetails(Owner owner, Address newAddress, Location newLocation) {
        if (owner == null) return false;
        if (newAddress != null) owner.setAddress(newAddress);
        if (newLocation != null) owner.setLocation(newLocation);
        ownerDao.update(owner);
        return true;
    }

    public boolean updateDeliveryLocation(Deliveryman dm, Location newLocation) {
        if (dm == null || newLocation == null) return false;
        dm.setLocation(newLocation);
        deliverymanDao.update(dm);
        return true;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(customerDao.getAll());
        allUsers.addAll(ownerDao.getAll());
        allUsers.addAll(deliverymanDao.getAll());
        return allUsers;
    }
}
