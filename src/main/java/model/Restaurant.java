package model;

import java.util.ArrayList;

public class Restaurant {

    private Address address;   // human-readable address (not used for distance)
    private Location location;// a coordinate system
    private String phone_number;
    private String title;
    private Owner owner;
    private ArrayList<Period> working_periods;
    private Menu menu;
    private RestaurantType type;

    public Restaurant(Address address, Location location, String phone_number, String title, Owner owner, ArrayList<Period> working_periods, Menu menu) {
        this.address = address;
        this.location = location;
        this.phone_number = phone_number;
        this.title = title;
        this.owner = owner;
        this.working_periods = working_periods;
        this.menu = menu;

    }

    public Restaurant() { // used for testing
    }
}

