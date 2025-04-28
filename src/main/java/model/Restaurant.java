package model;
import java.time.LocalTime;
import java.util.ArrayList;

public class Restaurant {
    private Address address;   // human-readable address (not used for distance)
    private Location location;// a coordinate system
    private String phone_number;
    private String title;
    private Owner owner;
    private ArrayList <Period> working_periods;
    private Menu menu;
    private RestaurantType type;

    public Restaurant(Address address, Location location, String title, Owner owner, RestaurantType type) {
        this.address = address;
        this.location = location;
        this.title = title;
        this.owner = owner;
        this.type = type;
    }
}
// maybe needs some modification
class Period{
    private LocalTime start_time;
    private LocalTime end_time;
    public Period (LocalTime start_time,LocalTime end_time){
    this.start_time=start_time;
    this.end_time=end_time;}
}
