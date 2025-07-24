package model;

import enums.ItemCategory;
import exception.InvalidInputException;
import exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;
    @Column (columnDefinition = "TEXT")
    private String description;
    private int count;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_hashtags", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @Lob
    private String imageBase64;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Embedded
    private Price price;

    @ManyToMany(mappedBy = "items", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Menu> menus = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    public Item() {}

    public Item(String title, String description, int priceValue, int count, ArrayList<String> hashtags, String imageBase64) {
        this.title = title;
        this.description = description;
        this.count = count;
        this.hashtags = hashtags;
        this.price = new Price(priceValue);
        this.imageBase64 = imageBase64;
    }

    public void setPrice(int price) throws InvalidInputException {
        if (price >= 0)
            this.price.setPrice(price);
        else
            throw new InvalidInputException(400, "invalid price");
    }

    public void decreaseCount(int quantity) {
        this.count -= quantity;
    }
    public void increaseCount(int quantity) {
        this.count += quantity;
    }



    public void addToMenu(Menu menu) {
        menus.add(menu);
    }

    public void removeFromMenu(int menuId) throws NotFoundException {
    }
}