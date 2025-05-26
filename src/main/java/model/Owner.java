package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("SELLER")
public class Owner extends User {

    @Embedded
    private Location location;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Restaurant restaurant;

    public Owner() {
        super();
        setRole(Role.SELLER);
    }

    public Owner( String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super( fullName, address, phoneNumber, email, password, Role.SELLER, profileImageBase64, bankName, accountNumber);
    }
}