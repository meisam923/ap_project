package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Column(unique = true, nullable = false, updatable = false)
    private String publicId = UUID.randomUUID().toString();
    private String fullName;
    private String address;
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    @Column(unique = true)
    private String email;
    private String password;
    private String profileImageBase64;
    private String bankName;
    private String accountNumber;
    @Column(nullable = false)
    private boolean isVerified = false;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, insertable = false, updatable = false)
    private Role role;

    public User() {}

    public User(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        this.fullName = fullName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.profileImageBase64 = profileImageBase64;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    public void addToWallet(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.walletBalance = this.walletBalance.add(amount);
        }
    }

    public void subtractFromWallet(BigDecimal amount) {
        if (amount != null && this.walletBalance.compareTo(amount) >= 0) {
            this.walletBalance = this.walletBalance.subtract(amount);
        } else {
            throw new IllegalStateException("Insufficient funds in wallet.");
        }
    }

}