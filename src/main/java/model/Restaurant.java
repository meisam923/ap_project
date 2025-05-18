package model;

import exception.NotAcceptableException;

import java.time.LocalTime;
import java.util.ArrayList;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private final Address address;// human-readable address (not used for distance)
    @Embedded
    private final Location location;// a coordinate system
    private String phone_number;
    private String title;
    @OneToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;
    @OneToMany
    private ArrayList<Period> working_periods;

    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;

    public Restaurant(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {
        validateField(address, location, phone_number, title, owner, category);
        this.address = address;
        this.location = location;
        this.phone_number = phone_number;
        this.title = title;
        this.owner = owner;
        this.working_periods = new ArrayList<>();
        this.category = RestaurantCategory.buildCategory(category);

    }

    public Restaurant() { // used for testing
        address = null;
        location = new Location(0,0);
    }

    public boolean setPeriod(LocalTime start, LocalTime end) {
        if (this.working_periods.size() == 2) {
            return false;
        }
        Period period = new Period(start, end);
        this.working_periods.add(period);
        return true;
    }

//    public void addItemS (String  title, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant,String type) throws NotAcceptableException {
//        Item new_item;
//        if (type.equals("Drink")) {
//            new_item=new Drink(title,description,price,count,hashtags,restaurant,ItemCategory.DRINK) ;
//            menu.addItem(new_item);
//            return;
//        }
//        else {
//            if (ItemCategory.buildCategory(type)==null) {
//                System.out.println("Invalid Category");
//                return ;
//            }
//            new_item=new Food(title,description,price,count,hashtags,restaurant,ItemCategory.buildCategory(type)) ;
//            menu.addItem(new_item);
//            return;
//
//        }

    //}

    public static void validateField(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {
        if ((address == null || location == null || phone_number == null || title == null || owner == null) ||
                (!phone_number.matches("0\\d{10}")) ||
                (!title.matches("(?i)^[a-z]{1,20}$") ||
                        (RestaurantCategory.buildCategory(category) == null)))
            throw new NotAcceptableException("invalid field");
    }

    public Owner getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

}

