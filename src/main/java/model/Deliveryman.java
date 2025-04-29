package model;

public class Deliveryman extends User {
    private Location location; // a coordinate system
    // the delivery man doesnt need address

    public Deliveryman(String first_name, String last_name, String phone_number, String email, String password, Location location) {
        super(first_name, last_name, phone_number, email, password, Role.DELIVERY_MAN);
        this.location = location;

    }

}
