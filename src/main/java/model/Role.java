package model;
<<<<<<< HEAD
public enum Role{
ADMIN("Admin"),
DELIVERYMAN("Delivaryman"),
CUSTOMER("Customer"),
SALESMAN("Salesman");
=======

public enum Role {
    ADMIN, DELIVERYMAN, CUSTOMER, OWNER;
>>>>>>> f4c67e09cd4d7d8ee01083f0c66dba1698071eea


    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
