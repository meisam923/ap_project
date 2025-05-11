package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

//singleton class :)
public class UserManager {
    private static UserManager instance;

    private final List<User> users = new ArrayList<>();
    private final Map<String, User> userByPublicId = new HashMap<>();

    // these lists might be useful later
    private final List<Customer> customers = new ArrayList<>();
    private final Map<String, Customer> customerByPublicId = new HashMap<>();
    private final List<Owner> owners = new ArrayList<>();
    private final Map<String, Owner> ownerByPublicId = new HashMap<>();
    private final List<Deliveryman> deliverymen = new ArrayList<>();
    private final Map<String, Deliveryman> deliverymenByPublicId = new HashMap<>();

    private UserManager() {
    }
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void addUser(User user) {
        if (user == null) return;

        // Add to general user list and map
        users.add(user);
        userByPublicId.put(user.getPublicId(), user);

        // Categorize and store based on role
        if (user instanceof Customer customer) {
            customers.add(customer);
            customerByPublicId.put(customer.getPublicId(), customer);
        } else if (user instanceof Owner owner) {
            owners.add(owner);
            ownerByPublicId.put(owner.getPublicId(), owner);
        } else if (user instanceof Deliveryman deliveryman) {
            deliverymen.add(deliveryman);
            deliverymenByPublicId.put(deliveryman.getPublicId(), deliveryman);
        }
    }


    //u can find a user by publicId and Email
    public User findByPublicId(String publicId) {
        return userByPublicId.get(publicId);
    }
    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    //reset password
    public void resetPassword(User user , String password) {
        if (user == null) return;
        user.setPassword(password);
    }

    //remove user
    public boolean removeUser(User user) {
        boolean removedMaster = users.remove(user);

        if (user instanceof Customer) {
            customers.remove(user);
        } else if (user instanceof Owner) {
            owners.remove(user);
        } else if (user instanceof Deliveryman) {
            deliverymen.remove(user);
        }

        return removedMaster;
    }

    //getter for allUsers
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }  //returns a copy instead of the user list itself (list can not be modified)

    public List<User> getUsers() {
        return users;
    }

    public Map<String, User> getUserByPublicId() {
        return userByPublicId;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public Map<String, Customer> getCustomerByPublicId() {
        return customerByPublicId;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public Map<String, Owner> getOwnerByPublicId() {
        return ownerByPublicId;
    }

    public List<Deliveryman> getDeliverymen() {
        return deliverymen;
    }

    public Map<String, Deliveryman> getDeliverymenByPublicId() {
        return deliverymenByPublicId;
    }

}

