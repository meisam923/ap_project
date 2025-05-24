package model;

import com.google.gson.annotations.SerializedName;
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
    private int id;

    @Column(unique = true, nullable = false, updatable = false)
    private String publicId = UUID.randomUUID().toString(); // UUID for external reference

    @SerializedName("full_name")
    private String fullName;

    private String address;

    @SerializedName("phone")
    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String password;

    @SerializedName("role")
    @Enumerated(EnumType.STRING)
    private Role role;

    private String profileImageBase64;

    private String bankName;

    private String accountNumber;

    private boolean isVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public User() {}

    public User(String fullName, String address, String phoneNumber, String email, String password, Role role, String profileImageBase64, String bankName, String accountNumber) {
        this.fullName = fullName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImageBase64 = profileImageBase64;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }
}
