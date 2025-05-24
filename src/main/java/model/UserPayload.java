package model;

import enums.Role;
import lombok.Getter;

@Getter
public class UserPayload {
    private final String publicId;
    private final String email;
    private final Role role;

    public UserPayload(String publicId, String email, Role role) {
        this.publicId = publicId;
        this.email    = email;
        this.role     = role;
    }

}
