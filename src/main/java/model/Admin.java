package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Role role = Role.ADMIN;

    private ArrayList<Long> notRegisteredRestaurantIds =new ArrayList<>();

    public void registerRestaurant(long id) {
        notRegisteredRestaurantIds.add(id);
    }
    public void removeRestaurant(long id) {
        notRegisteredRestaurantIds.remove(id);
    }
}
