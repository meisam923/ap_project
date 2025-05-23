package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "deliverymen")
@PrimaryKeyJoinColumn(name = "id")
public class Deliveryman extends User {

    @Embedded
    private Location location; // a coordinate system

    @OneToMany(mappedBy = "deliveryman", cascade = CascadeType.ALL, orphanRemoval = true)
    private ArrayList<Order> ordersAssigned = new ArrayList<>();

    public Deliveryman() {
        super();
        setRole(Role.DELIVERY_MAN);
    }

    public Deliveryman(String firstName,
                       String lastName,
                       String phoneNumber,
                       String email,
                       String password,
                       Location location) {
        super(firstName, lastName, phoneNumber, email, password, Role.DELIVERY_MAN);
        this.location = location;
    }

}
