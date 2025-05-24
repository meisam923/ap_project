package model;

import enums.ItemCategory;
import exception.NotAcceptableException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column (columnDefinition = "TEXT")
    private String description;
    private int count;

    @ElementCollection
    @CollectionTable(name = "item_hashtags", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Embedded
    private Price price;

    @OneToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
    @Lob
    private byte[] image;

    public Item() {
    }

    public Item(String title,
                String description,
                int priceValue,
                int count,
                ArrayList<String> hashtags,
                ItemCategory category) throws NotAcceptableException {
        validateField(title, description, priceValue, count, hashtags);
        this.title = title;
        this.description = description;
        this.count = count;
        this.hashtags = hashtags;
        this.category = category;
        this.price = new Price(priceValue);
    }

    public static void validateField(String title, String description, int price, int count, ArrayList<String> hashtags) throws NotAcceptableException {
        if (title == null || price <= 0 || count < 0 || hashtags == null ||
                (!title.matches("(?i)^[a-z]{1,20}$") ||
                        (!description.matches("(?i)^[a-z]{0,50}$"))))
            throw new NotAcceptableException("invalid field");
    }

    public void setHashtags(ArrayList<String> hashtags) {
        this.hashtags = hashtags;
    }


    public void setPrice(int price) throws NotAcceptableException {
        if (price >= 0)
            this.price.setPrice(price);
        else
            throw new NotAcceptableException("invalid argument");
    }

    public void decreaseCount(int quantity) {
        this.count -= quantity;
    }
    public void increaseCount(int quantity) {
        this.count += quantity;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }


    public void setPrice(Price price) {
        this.price = price;
    }

}


