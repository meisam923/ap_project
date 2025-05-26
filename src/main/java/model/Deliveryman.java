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
@DiscriminatorValue("COURIER")
public class Deliveryman extends User {

    @Embedded
    private Location location;

    @OneToMany(mappedBy = "deliveryman", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> ordersAssigned = new ArrayList<>();

    public Deliveryman() {
        super();
        setRole(Role.COURIER);
    }

    public Deliveryman(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super(fullName, address, phoneNumber, email, password, Role.COURIER, profileImageBase64, bankName, accountNumber);
    }
}