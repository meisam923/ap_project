package model;

public class Customer extends User {
    private Address address;   // human-readable address (not used for distance)
    private Location location; // a coordinate system


    public Customer(String first_name, String last_name, String phone_number, String email, String password, Address address, Location location) {
        super(first_name, last_name, phone_number, email, password, Role.CUSTOMER);
        this.address = address;
        this.location = location;

    }
}
