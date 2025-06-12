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

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Restaurant restaurant;

    public Owner() {
        super();
    }

    public Owner(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
    }
}