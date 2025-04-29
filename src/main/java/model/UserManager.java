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
        // TODO: conditions to check if the user can be added
        users.add(user);
        userByPublicId.put(user.getPublicId(), user);
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

    //getter for allUsers
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}

