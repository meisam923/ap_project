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


    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Embedded
    private Price price;

    @OneToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
    @Lob
    private byte[] image;

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

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

    public Price getPrice() {
        return price;
    }

    public static void validateField(String title, String description, int price, int count, ArrayList<String> hashtags) throws NotAcceptableException {
        if (title == null || price <= 0 || count < 0 || hashtags == null ||
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
        else
            throw new NotAcceptableException("invalid argument");
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setDiscount(int percentage, LocalDateTime expiration) {
        price.setDiscount(percentage, expiration);
    }

    public void decreaseCount(int quantity) {
        this.count -= quantity;
    }
    public void increaseCount(int quantity) {
        this.count += quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getCount() {
        return count;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }


    public ItemCategory getCategory() {
        return category;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public byte[] getImage() {
        return image;
    }
}


