package model;

import java.util.ArrayList;

public class Order {
    private Customer customer;
    private Restaurant restaurant;
    private ArrayList<Item> items;
    private Deliveryman deliveryman;

    public Order(Customer customer, Restaurant restaurant, ArrayList<Item> items) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = items;
        //don't remember to intial the deliveryman
    }
}
