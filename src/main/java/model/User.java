package model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    public User() {
    }

    @Column(unique = true)
    private String publicId; // UUID used for referencing in collections
    @Column(unique = true)
    private String sessionToken;
    @Column
    private String first_name;
    @Column
    private String last_name;
    @Column(unique = true)
    private String phone_number;
    @Column(unique = true)
    private String email;
    @Column
    private String password;
    @Column
    @Enumerated(EnumType.STRING)
    private Role role;
    @Lob
    @Column
    private byte[] image;  // Storing the image as binary data


    public User(String first_name, String last_name, String phone_number, String email, String password, Role role) {
        this.publicId = UUID.randomUUID().toString();
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public void setPhoneNumber(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
