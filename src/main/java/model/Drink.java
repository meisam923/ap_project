package model;

import exception.NotAcceptableException;
import jakarta.persistence.Embeddable;

import java.util.ArrayList;
@Embeddable
public class Drink extends Item{
    public Drink(String name, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant,ItemCategory category) throws NotAcceptableException {
        super(name, description, price, count, hashtags, restaurant,category);
    }

    public Drink() {

    }
}
