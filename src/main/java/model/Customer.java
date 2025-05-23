package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@PrimaryKeyJoinColumn(name = "id")
public class Customer extends User {

    @Embedded
    private Address address;   // human-readable address

    @Embedded
    private Location location; // a coordinate system

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> ordersAssigned = new ArrayList<>();

    public Customer() {
        super();
        setRole(Role.CUSTOMER);
    }

    public Customer(String firstName,
                    String lastName,
                    String phoneNumber,
                    String email,
                    String password,
                    Address address,
                    Location location) {
        super(firstName, lastName, phoneNumber, email, password, Role.CUSTOMER);
        this.address = address;
        this.location = location;
    }

    public void setOrdersAssigned(ArrayList<Order> ordersAssigned) {
        this.ordersAssigned = ordersAssigned;
    }
}
