package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Entity
public class Admin implements RestaurantObserver {

    @Id
    private Long id;

    private Role role = Role.ADMIN;
    private ArrayList<Restaurant> notRegisteredRestaurants =new ArrayList<>();

    public void registerRestaurant(@NotNull Restaurant restaurant) {
        notRegisteredRestaurants.add(restaurant);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
