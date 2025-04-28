package model;

public class Owner extends User {
    private Address address;   // human-readable address (not used for distance)
    private Location location; // a coordinate system


    public Owner(String first_name, String last_name, String phone_number, String email, String password, Role role, Address address, Location location) {
        super(first_name, last_name, phone_number, email, password, role);
        this.address = address;
        this.location = location;
    }
}
