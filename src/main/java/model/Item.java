package model;

import exception.NotAcceptableException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "items")
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private int count;

    @ElementCollection
    @CollectionTable(name = "item_hashtags", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", updatable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Embedded
    private Price price;

    @Lob
    private byte[] image;

    public Item() {
    }

    public Item(String title,
                String description,
                int priceValue,
                int count,
                ArrayList<String> hashtags,
                Restaurant restaurant,
                ItemCategory category) throws NotAcceptableException {
        validateField(title, description, priceValue, count, hashtags, restaurant);
        this.title = title;
        this.description = description;
        this.count = count;
        this.hashtags = hashtags;
        this.restaurant = restaurant;
        this.category = category;
        this.price = new Price(priceValue);
    }

    public Price getPrice() {
        return price;
    }

    public static void validateField(String title, String description, int price, int count, ArrayList<String> hashtags, Restaurant restaurant) throws NotAcceptableException {
        if (title == null || price <= 0 || count < 0 || hashtags == null || restaurant == null ||
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

//    public void setImage(ImageIcon image) {
//        this.image = image;
//    }

    public void setDiscount(int percentage, LocalDateTime expiration) {
        price.setDiscount(percentage, expiration);
    }
}

@Embeddable
class Discount {

    @Column(name = "discount_percentage")
    private int percentage;

    @Column(name = "discount_expiration")
    private LocalDateTime expiration;

    private boolean isActive = false;

    public Discount() {
        // Required by JPA
    }

    public Discount(int percentage, LocalDateTime until_when) {
        this.percentage = percentage;
        this.expiration = until_when;
        this.isActive = true;
    }

    public boolean isActive(LocalDateTime now) {
        return now.isBefore(expiration) && isActive;
    }

    public int getPercentage() {
        return percentage;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }
}
