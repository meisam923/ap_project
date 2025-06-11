package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Menu {

    private String title;
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "menu_items",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Item> items = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id")
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

    public void removeItem(int itemId) throws NotFoundException {
        Iterator<Item> iterator = items.iterator();
        Item item = null;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getId() == itemId) {
                iterator.remove();
                return;
            }
            item=null;
        }
        if (item==null){throw new NotFoundException(404, "item is not  found in menu");}
    }
}

