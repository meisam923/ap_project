package model;
import exception.NotAcceptableException;
import java.time.LocalTime;
import java.util.ArrayList;

public class Restaurant {

    private final Address address;// human-readable address (not used for distance)
    private final Location location;// a coordinate system
    private String phone_number; ;
    private String title;
    private Owner owner;
    private ArrayList<Period> working_periods;
    private Menu menu;
    private ArrayList<RestaurantCategory> Categories = new ArrayList<>();

    public Restaurant(Address address, Location location, String phone_number, String title, Owner owner) throws NotAcceptableException {
        validateField(address ,location,phone_number,title,owner);
        this.address = address;
        this.location = location;
        this.phone_number = phone_number;
        this.title = title;
        this.owner = owner;
        this.working_periods = new ArrayList<>();

    }

    public void add_Menu(Menu menu) {
        this.menu = menu;
    }

    public Restaurant() { // used for testing
        address = null;
        location = null;
    }

    public boolean setPeriod(LocalTime start, LocalTime end) {
        if (this.working_periods.size() == 2) {
            return false;
        }
        Period period = new Period(start, end);
        this.working_periods.add(period);
        return true;
    }

    public void addItemS (String  title, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant,String type) throws NotAcceptableException {
        Item new_item;
        if (type.equals("Food")) {
            new_item=new Food(title,description,price,count,hashtags,restaurant) ;
            menu.addItem(new_item);
            return;
        }
        else if (type.equals("Drink")) {
            new_item=new Drink(title,description,price,count,hashtags,restaurant) ;
            menu.addItem(new_item);
            return;
        }
        return;

        }

    public static void validateField(Address address, Location location, String phone_number, String title, Owner owner) throws NotAcceptableException {
        if ((address == null || location == null || phone_number == null || title == null || owner == null) ||
                (!phone_number.matches("0\\d{10}")) ||
                (!title.matches("(?i)^[a-z]{1,20}$")))
            throw new NotAcceptableException("invalid field");
    }

}

