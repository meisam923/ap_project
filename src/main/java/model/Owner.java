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
    private Address address;   // human-readable address

    @Embedded
    private Location location; // a coordinate system

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Restaurant restaurant;

    public Owner() {
        super();
        setRole(Role.OWNER);
    }

    public Owner(String firstName,
                 String lastName,
                 String phoneNumber,
                 String email,
                 String password,
                 Address address,
                 Location location) {
        super(firstName, lastName, phoneNumber, email, password, Role.OWNER);
        this.address = address;
        this.location = location;
    }


}
