package model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Embeddable
public class Menu {

    @OneToMany
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }
}

