package model;

import enums.Role;
import jakarta.persistence.*;
import observers.RestaurantObserver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Role role = Role.ADMIN;

    //private ArrayList<Long> notRegisteredRestaurantIds =new ArrayList<>();

//    public void registerRestaurant(long id) {
//
//        notRegisteredRestaurantIds.add(id);
//    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

//    public ArrayList<Long> getNotRegisteredRestaurantIds() {
//        return notRegisteredRestaurantIds;
//    }
}
