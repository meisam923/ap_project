package model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.SerializedName;
import enums.RestaurantCategory;
import enums.RestaurantStatus;
import exception.NotAcceptableException;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Embedded
    private String  address;// human-readable address (not used for distance)
    @Embedded
    private Location location;// a coordinate system

    @SerializedName("phone")
    @Column (unique = true)
    private String phone_number;

    @SerializedName("name")
    private String title;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToOne
    private Menu menu;

    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    private int tax_fee;

    private int additional_fee;

    private String logoBase64;

    public Restaurant(String address, String phone_number, String title, Owner owner)  {
        //validateField(address, location, phone_number, title, owner, category);
        this.address = address;
        this.phone_number = phone_number;
        this.title = title;
        this.owner = owner;
        this.status = RestaurantStatus.WAITING;

    }

    public Restaurant() { // used for testing
        address = null;
        location = new Location(0,0);
    }



//    public static void validateField(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {
//        if ((address == null || location == null || phone_number == null || title == null || owner == null) ||
//                (!phone_number.matches("0\\d{10}")) ||
//                (!title.matches("(?i)^[a-z]{1,20}$") ||
//                        (RestaurantCategory.buildCategory(category) == null)))
//            throw new NotAcceptableException("invalid field");
//    }
}