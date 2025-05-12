package model;

public class Owner extends User {
    private Address address;   // human-readable address (not used for distance)
    private Location location; // a coordinate system
    private Restaurant restaurant=null;

    public Owner(String first_name, String last_name, String phone_number, String email, String password, Address address, Location location) {
        super(first_name, last_name, phone_number, email, password, Role.OWNER);
        this.address = address;
        this.location = location;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
}
