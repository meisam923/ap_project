package model;

import enums.ItemCategory;
import exception.InvalidInputException;
import exception.NotAcceptableException;
import exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.engine.internal.Nullability;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static jakarta.persistence.FetchType.*;

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

    public Item() {
    }
    public Item(String title,
                String description,
                int priceValue,
                int count,
                ArrayList<String> hashtags,String imageBase64) {
                this.title = title;
                this.description = description;
        this.count = count;
        this.hashtags = hashtags;
        this.price = new Price(priceValue);
        this.imageBase64 = imageBase64;
    }
    public void setPrice(int price) throws  InvalidInputException {
        if (price >= 0)
            this.price.setPrice(price);
        else
            throw new InvalidInputException(400,"invalid price");
    }

    public void decreaseCount(int quantity) {
        this.count -= quantity;
    }
    public void increaseCount(int quantity) {
        this.count += quantity;
    }
    public Restaurant getRestaurant() {
        return this.menus.getFirst().getRestaurant();
    }
    public void addToMenu(Menu menu) {
        menus.add(menu);
    }
    public void removeFromMenu(int menuId) throws NotFoundException {
        if (this.menus == null) {
            throw new NotFoundException(404, "Menu not found in item.");
        }
        boolean removed = this.menus.removeIf(menu -> menu.getId() == menuId);

        if (!removed) {
            throw new NotFoundException(404, "Menu with ID " + menuId + " not associated with this item.");
        }
    }
}


