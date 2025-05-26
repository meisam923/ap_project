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
@DiscriminatorValue("BUYER")
public class Customer extends User {

    @Embedded
    private Location location;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> ordersAssigned = new ArrayList<>();

    public Customer() {
        super();
        setRole(Role.BUYER);
    }

    public Customer(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super(fullName, address, phoneNumber, email, password, Role.BUYER, profileImageBase64, bankName, accountNumber);
    }
}