package Services;


import dao.CustomerDao;
import dao.DeliverymanDao;
import dao.OwnerDao;
import enums.Role;
import model.*;

import org.jetbrains.annotations.NotNull;

import java.util.*;



public class UserService {
    private static UserService instance;

    private final CustomerDao customerDao = new CustomerDao();
    private final OwnerDao ownerDao = new OwnerDao();
    private final DeliverymanDao deliverymanDao = new DeliverymanDao();

    private UserService() {
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }


    public Optional<User> addUser(@NotNull User user) {
        try {
            switch (user) {
                case Customer customer -> customerDao.save(customer);
                case Owner owner -> ownerDao.save(owner);
                case Deliveryman deliveryman -> deliverymanDao.save(deliveryman);
                default -> throw new IllegalArgumentException("Unknown user type, cannot add: " + user.getClass().getName());
            }
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) return Optional.empty();
        User user = customerDao.findByPublicId(publicId);
        if (user != null) return Optional.of(user);
        user = ownerDao.findByPublicId(publicId);
        if (user != null) return Optional.of(user);
        user = deliverymanDao.findByPublicId(publicId);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        User user = customerDao.findByEmail(email);
        if (user != null) return Optional.of(user);
        user = ownerDao.findByEmail(email);
        if (user != null) return Optional.of(user);
        user = deliverymanDao.findByEmail(email);
        return Optional.ofNullable(user);
    }


    public Optional<User> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) return Optional.empty();
        User user = customerDao.findByPhone(phone);
        if (user != null) return Optional.of(user);
        user = ownerDao.findByPhone(phone);
        if (user != null) return Optional.of(user);
        user = deliverymanDao.findByPhone(phone);
        return Optional.ofNullable(user);
    }


    public boolean resetPassword(@NotNull User user, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank.");
        }
        user.setPassword(newPassword);

        switch (user) {
            case Customer customer -> customerDao.update(customer);
            case Owner owner -> ownerDao.update(owner);
            case Deliveryman deliveryman -> deliverymanDao.update(deliveryman);
            default -> throw new IllegalArgumentException("Unknown user type for password reset: " + user.getClass().getName());
        }
        return true;
    }

    public boolean removeUser(@NotNull User user) {
        switch (user) {
            case Customer customer -> customerDao.delete(customer);
            case Owner owner -> ownerDao.delete(owner);
            case Deliveryman deliveryman -> deliverymanDao.delete(deliveryman);
            default -> throw new IllegalArgumentException("Unknown user type for removal: " + user.getClass().getName());
        }
        return true;
    }


    public boolean updateBasicProfile(@NotNull User user, String fullName, String phoneNumber, String email, String password) {
        boolean changed = false;
        if (fullName != null && !fullName.isBlank() && !fullName.equals(user.getFullName())) {
            user.setFullName(fullName);
            changed = true;
        }
        if (phoneNumber != null && !phoneNumber.isBlank() && !phoneNumber.equals(user.getPhoneNumber())) {
            Optional<User> existingUserWithNewPhone = findByPhone(phoneNumber);
            if (existingUserWithNewPhone.isPresent() && !existingUserWithNewPhone.get().getPublicId().equals(user.getPublicId())) {
                throw new IllegalArgumentException("Phone number '" + phoneNumber + "' is already in use by another account.");
            }
            user.setPhoneNumber(phoneNumber);
            changed = true;
        }
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            Optional<User> existingUserWithNewEmail = findByEmail(email);
            if (existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getPublicId().equals(user.getPublicId())) {
                throw new IllegalArgumentException("Email '" + email + "' is already in use by another account.");
            }
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
                default -> throw new IllegalArgumentException("Unknown user type for profile update: " + user.getClass().getName());
            }
        }
        return changed;
    }

    public boolean updateCustomerDetails(@NotNull Customer customer, String newAddress, Location newLocation) {
        boolean changed = false;
        if (newAddress != null && !newAddress.isBlank() && !newAddress.equals(customer.getAddress())) {
            customer.setAddress(newAddress);
            changed = true;
        }
        if (newLocation != null && (customer.getLocation() == null || !newLocation.equals(customer.getLocation()))) {
            customer.setLocation(newLocation);
            changed = true;
        }
        if (changed) customerDao.update(customer);
        return changed;
    }

    public boolean updateOwnerDetails(@NotNull Owner owner, String newAddress, Location newLocation) {
        boolean changed = false;
        if (newAddress != null && !newAddress.isBlank() && !newAddress.equals(owner.getAddress())) {
            owner.setAddress(newAddress);
            changed = true;
        }
        if (newLocation != null && (owner.getLocation() == null || !newLocation.equals(owner.getLocation()))) {
            owner.setLocation(newLocation);
            changed = true;
        }
        if (changed) ownerDao.update(owner);
        return changed;
    }

    public boolean updateDeliveryLocation(@NotNull Deliveryman dm, Location newLocation) {
        boolean changed = false;
        if (newLocation != null && (dm.getLocation() == null || !newLocation.equals(dm.getLocation()))) {
            dm.setLocation(newLocation);
            changed = true;
        }
        if (changed) deliverymanDao.update(dm);
        return changed;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(customerDao.getAll());
        allUsers.addAll(ownerDao.getAll());
        allUsers.addAll(deliverymanDao.getAll());
        return allUsers;
    }


    public static class UserFactory {
        public static User createUser(
                @NotNull Role role,
                String fullName,
                String phoneNumber,
                String email,
                String password,
                String bankName,
                String accountNumber,
                String address,
                Restaurant restaurant,
                String profileImageBase64
        ) {
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new IllegalArgumentException("Email and password are required for user creation.");
            }

            return switch (role) {
                case BUYER -> new Customer(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
                case SELLER ->
                    new Owner(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);

                case COURIER ->
                        new Deliveryman(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
                default -> null;
            };
        }
    }
}