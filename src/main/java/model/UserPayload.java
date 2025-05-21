package model;   // or simply `model`

import enums.Role;

public class UserPayload {
    private final String publicId;
    private final String email;
    private final Role role;

    public UserPayload(String publicId, String email, Role role) {
        this.publicId = publicId;
        this.email    = email;
        this.role     = role;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
