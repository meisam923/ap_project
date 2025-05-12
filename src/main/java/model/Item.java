package model;

import exception.NotAcceptableException;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class Item {
    private String title;
    private String description;
    private int count;
    private ArrayList<String> hashtags;
    private final Restaurant restaurant;
    ItemCategory category;
    Price price;
    ImageIcon image;
    private int discount;

    public Item(String title, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant,ItemCategory category) throws NotAcceptableException {
        validateField(title,description,price ,count,hashtags,restaurant);
        this.title = title;
        this.description = description;
        this.price=new Price(price);
        this.count = count;
        this.hashtags = hashtags;
        this.restaurant = restaurant;
        this.category = category;

    }
    public Price getPrice() {
        return price;
    }
    public static void validateField (String title, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant) throws NotAcceptableException {
    if (title == null || price <=0 || count <0 || hashtags == null ||  restaurant == null ||
            (!title.matches("(?i)^[a-z]{1,20}$") ||
            (!description.matches("(?i)^[a-z]{0,50}$"))))
            throw new NotAcceptableException("invalid field");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setHashtags(ArrayList<String> hashtags) {
        this.hashtags = hashtags;
    }


    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public void setPrice(int price) throws NotAcceptableException {
        if (price >= 0)
        this.price.setPrice(price);
        throw new NotAcceptableException("invalid argument");
    }

    public void setImage(ImageIcon image) {
        this.image = image;
    }
    public void setDiscount(int percentage , LocalDateTime expiration) {
        price.setDiscount(percentage, expiration);
    }
}
class Discount  {
    private int percentage;
    private LocalDateTime expiration;
    private boolean isActive=false;
    public Discount(int percentage, LocalDateTime until_when) {
        this.percentage = percentage;
        this.expiration = until_when;
        isActive=true;
    }
    public boolean isActive(LocalDateTime now) {
        if (now.isAfter(expiration)){
        return false;}
        return isActive;
    }
    public int getPercentage() {
        return percentage;
    }
}
