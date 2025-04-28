package model;
public enum Role{
ADMIN("Admin"),
DELIVERYMAN("Delivaryman"),
CUSTOMER("Customer"),
SALESMAN("Salesman");


    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
