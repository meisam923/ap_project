package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "owners")
@PrimaryKeyJoinColumn(name = "id")
public class Owner extends User {

    @Embedded
    private Location location; // a coordinate system

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Restaurant restaurant;

    public Owner() {
        super();
        setRole(Role.OWNER);
    }

    public Owner( String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber, Restaurant restaurant) {
        super( fullName, address, phoneNumber, email, password, Role.OWNER, profileImageBase64, bankName, accountNumber);
        this.restaurant = restaurant;
    }
}
