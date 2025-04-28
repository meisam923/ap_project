package model;
import java.time.LocalTime;
import java.util.ArrayList;

public class Restaurant {
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
    }
}
