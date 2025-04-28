package model;

public class Restaurant {
    private Address address;   // human-readable address (not used for distance)
    private Location location; // a coordinate system

    private String title;
    private Owner owner;

    private RestaurantType type;

    public Restaurant(Address address, Location location, String title, Owner owner, RestaurantType type) {
        this.address = address;
        this.location = location;
        this.title = title;
        this.owner = owner;
        this.type = type;
    }
}
