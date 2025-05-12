package model;

import java.util.ArrayList;

public class Cart {
    private ArrayList<Item> menuItems;
    private Price price;

    public Cart() {

        menuItems = new ArrayList<Item>();
        price = new Price(0);
    }
//    public void adaItem(Item item) {
//        menuItems.add(item);
//        price.sumPrice(item.getPrice());
//    }
}
