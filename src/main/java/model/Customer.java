package model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    public List<Order> getOrdersAssigned() {
        return ordersAssigned;
    }

    public void setOrdersAssigned(ArrayList<Order> ordersAssigned) {
        this.ordersAssigned = ordersAssigned;
    }
}
