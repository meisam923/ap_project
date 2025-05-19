package model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Menu {

    @OneToMany
    private List<Item> items = new ArrayList<>();
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @OneToOne(optional = false)
    @JoinColumn(name = "restaurant_id", updatable = false)
    private Restaurant restaurant;
    private Long id;

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

