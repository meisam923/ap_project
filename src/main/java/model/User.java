package model;

import java.util.UUID;

public abstract class User {

    private final String publicId; // UUID used for referencing in collections

    private String first_name;
    private String last_name;
    private String phone_number;
    private String email;
    private String password;
    private Role role;

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
