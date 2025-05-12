package model;

import exception.NotAcceptableException;

import java.util.ArrayList;

public class Food extends Item{
    public Food(String name, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant,ItemCategory category) throws NotAcceptableException {
        super(name, description, price, count, hashtags, restaurant,category);
    }
}
