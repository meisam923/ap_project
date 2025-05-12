package model;

import exception.NotAcceptableException;

import java.util.ArrayList;

public class Drink extends Item{
    public Drink(String name, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant) throws NotAcceptableException {
        super(name, description, price, count, hashtags, restaurant);
    }
}
