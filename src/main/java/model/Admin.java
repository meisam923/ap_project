package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Entity
public class Admin implements RestaurantObserver {

    @Id
    private Long id;

    private Role role = Role.ADMIN;
    private ArrayList<Restaurant> notregisteredrestaurants=new ArrayList<>();

    public void registerRestaurant(Restaurant restaurant) {
        notregisteredrestaurants.add(restaurant);
        return;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
