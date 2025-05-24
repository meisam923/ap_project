package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Setter
@Getter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class User {

    // Getters & setters
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String publicId = UUID.randomUUID().toString(); // UUID for external reference

    private String firstName;

    private String lastName;

    private BankInfo bankInfo;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Lob
    private byte[] image; // Profile picture

    private boolean isVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public User() {}

    public User(String firstName, String lastName, String phoneNumber, String email, String password, Role role, BankInfo bankInfo) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = role;
        this.bankInfo = bankInfo;
    }

}
