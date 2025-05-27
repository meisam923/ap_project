package model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Menu {

    private String title;
    @OneToMany
    private List<Item> items = new ArrayList<>();

    @OneToOne(optional = false)
    @JoinColumn(name = "restaurant_id", updatable = false)
    private Restaurant restaurant;
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;

    public Menu(Restaurant restaurant,String title) {
        this.restaurant = restaurant;
        this.title = title;
    }

    protected Menu() {}

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

}

