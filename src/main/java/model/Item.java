package model;

import java.util.ArrayList;

public abstract class Item {
    private String name;
    private String description;
    private int count;
    private ArrayList<String> hashtags;
    private Restaurant restaurant;
    ItemCategory category;
    Price price;
    //image

    public Item(String name, String description, int price, int count, String hashtags, Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price=new Price(price);
        this.count = count;
        this.hashtags = new ArrayList<String>();
        this.restaurant = restaurant;

    }
    public Price getPrice() {
        return price;
    }

}
