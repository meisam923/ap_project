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

    @OneToMany(mappedBy = "deliveryman", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> ordersAssigned = new ArrayList<>();

    public Deliveryman() {
        super();
    }

    public Deliveryman(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
    }
}