package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Admin implements RestaurantObserver {

    @Id
    private Long id;

    private Role role = Role.ADMIN;

    public boolean registerRestaurant(Restaurant restaurant,Owner owner) {
        return true;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
