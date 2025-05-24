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
    private Location location; // a coordinate system

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> ordersAssigned = new ArrayList<>();

    public Customer() {
        super();
        setRole(Role.CUSTOMER);
    }

    public Customer(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super(fullName, address, phoneNumber, email, password, Role.CUSTOMER, profileImageBase64, bankName, accountNumber);
    }

}
