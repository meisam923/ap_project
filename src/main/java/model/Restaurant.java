package model;

import enums.RestaurantCategory;
import enums.RestaurantStatus;
import exception.NotAcceptableException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column (unique = true)
    private String phone_number;

    private String title;
    @OneToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Period> periods;
    @OneToOne
    private Menu menu;
    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;
    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    public Restaurant(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {
        validateField(address, location, phone_number, title, owner, category);
        this.address = address;
        this.location = location;
        this.phone_number = phone_number;
        this.title = title;
        this.owner = owner;
        this.periods = new ArrayList<>();
        this.category = RestaurantCategory.buildCategory(category);
        this.status = RestaurantStatus.WAITING;

    }

    public Restaurant() { // used for testing
        address = null;
        location = new Location(0,0);
    }

    public boolean setPeriod(LocalTime start, LocalTime end) {
        if (this.periods.size() == 2) {
            return false;
        }
        Period period = new Period(start, end,this);
        this.periods.add(period);
        return true;
    }


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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public Location getLocation() {
        return location;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<Period> getWorking_periods() {
        return periods;
    }

    public void setPeriods(ArrayList<Period> working_periods) {
        this.periods = working_periods;
    }

    public RestaurantCategory getCategory() {
        return category;
    }

    public void setCategory(RestaurantCategory category) {
        this.category = category;
    }
}

