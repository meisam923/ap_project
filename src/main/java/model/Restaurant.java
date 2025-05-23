package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import enums.RestaurantCategory;
import enums.RestaurantStatus;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Address address;// human-readable address (not used for distance)

    @Embedded
    private Location location;// a coordinate system

    @SerializedName("phone")
    @Column(unique = true)
    private String phone_number;

    @SerializedName("name")
    private String title;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;


    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)

    private List<Period> periods;

    @OneToOne
    private Menu menu;

    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    private int tax_fee;

    private int additional_fee;

    private String logoBase64;

    public Restaurant(Address address, Location location, String phone_number, String title, Owner owner, String category) {
        //validateField(address, location, phone_number, title, owner, category);
        this.address = address;
        this.location = location;
        this.phone_number = phone_number;
        this.title = title;
        this.owner = owner;
        this.periods = new ArrayList<>();
        this.category = RestaurantCategory.buildCategory(category);
        this.additional_fee = 0;
        this.tax_fee = 0;
        this.status = RestaurantStatus.WAITING;

    }

    public Restaurant() { // used for testing
        address = null;
        location = new Location(0, 0);
        this.periods = new ArrayList<>();
    }

    public boolean setPeriod(LocalTime start, LocalTime end) {
        if (this.periods.size() == 2) {
            return false;
        }
        Period period = new Period(start, end, this);
        this.periods.add(period);
        return true;
    }


//    public static void validateField(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {
//        if ((address == null || location == null || phone_number == null || title == null || owner == null) ||
//                (!phone_number.matches("0\\d{10}")) ||
//                (!title.matches("(?i)^[a-z]{1,20}$") ||
//                        (RestaurantCategory.buildCategory(category) == null)))
//            throw new NotAcceptableException("invalid field");
//    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public RestaurantCategory getCategory() {
        return category;
    }

    public void setCategory(RestaurantCategory category) {
        this.category = category;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public void setStatus(RestaurantStatus status) {
        this.status = status;
    }

    public int getTax_fee() {
        return tax_fee;
    }

    public void setTax_fee(int tax_fee) {
        this.tax_fee = tax_fee;
    }

    public int getAdditional_fee() {
        return additional_fee;
    }

    public void setAdditional_fee(int additional_fee) {
        this.additional_fee = additional_fee;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }
}