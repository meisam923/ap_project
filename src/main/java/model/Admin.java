package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Entity
public class Admin{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, insertable = false, updatable = false)
    private Role role = Role.ADMIN;
    private ArrayList<Long> notRegisteredRestaurantIds = new ArrayList<>();

    public Admin() {
    }

    public void registerRestaurant(long id) {
        notRegisteredRestaurantIds.add(id);
    }

    public void removeRestaurant(long id) {
        notRegisteredRestaurantIds.remove(id);
    }
}
