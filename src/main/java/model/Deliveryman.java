package model;

import java.util.ArrayList;

public class Deliveryman extends User {
    private Location location; // a coordinate system
    private ArrayList<Order> ordersAssigned;
    // the delivery man doesnt need address

    public Deliveryman(String first_name, String last_name, String phone_number, String email, String password, Location location) {
        super(first_name, last_name, phone_number, email, password, Role.DELIVERY_MAN);
        this.location = location;

    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ArrayList<Order> getOrdersAssigned() {
        return ordersAssigned;
    }

    public void setOrdersAssigned(ArrayList<Order> ordersAssigned) {
        this.ordersAssigned = ordersAssigned;
    }
}
