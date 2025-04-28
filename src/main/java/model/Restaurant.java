package model;
import java.time.LocalTime;
import java.util.ArrayList;

public class Restaurant {
<<<<<<< HEAD
    private String name ;
    private String address;
    private String cordination_location;
    private String number;
    private ArrayList <Period> working_periods;
    private Salesman salesman ;
    private Menu menu;
    private ArrayList<FoodCategory> categories;
    public Restaurant(String name, String address, String cordination_location,Salesman salesman) {
        this.name=name;
        this.address=address;
        this.cordination_location=cordination_location;
        this.salesman=salesman;
        working_periods=new ArrayList<Period>();
        menu=new Menu();
        categories=new ArrayList<>();

    }
}
// maybe needs some modification
class Period{
    private LocalTime start_time;
    private LocalTime end_time;
    public Period (LocalTime start_time,LocalTime end_time){
    this.start_time=start_time;
    this.end_time=end_time;
=======
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
>>>>>>> f4c67e09cd4d7d8ee01083f0c66dba1698071eea
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
