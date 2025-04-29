package model;

import java.util.ArrayList;

public class Food {
    private String name;
    private String description;
    private int price;
    private int count;
    private ArrayList<String> hashtags;
    private Restaurant restaurant;
    FoodCategory category;
    //image

    public Food(String name, String description, int price, int count, String hashtags, Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.count = count;
        this.hashtags = new ArrayList<String>();
        this.restaurant = restaurant;

    }

}
