package model;

public class Food extends Item{
    public Food(String name, String description, int price, int count, String hashtags, Restaurant restaurant) {
        super(name, description, price, count, hashtags, restaurant);
    }
}
