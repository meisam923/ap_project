package model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Cart {
    private ArrayList<Item> menuItems;
    private Price price; // unused rn
    private long discountedPrice;
    private long totalPrice;
    private long discountAmount;
    private LocalDateTime now;

    public Cart() {
        menuItems = new ArrayList<>();
        price = new Price(0);
        discountedPrice = 0;
        totalPrice = 0;
        discountAmount = 0;
        now = LocalDateTime.now();
    }

    public void addItem(Item item) {
        menuItems.add(item);
        totalPrice += item.getPrice().getPriceWithoutDiscount();
        discountedPrice += item.getPrice().getPriceWithDiscount(now);
        discountAmount += item.getPrice().getDiscountedAmount(now);
    }

    public void removeItem(Item item) {
        menuItems.remove(item);
        totalPrice -= item.getPrice().getPriceWithoutDiscount();
        discountedPrice -= item.getPrice().getPriceWithDiscount(now);
        discountAmount -= item.getPrice().getDiscountedAmount(now);
    }

    public ArrayList<Item> getMenuItems() {
        return menuItems;
    }

    public String menuItemsToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Item item : menuItems) {
            stringBuilder.append(item.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public long getDiscountedPrice() {
        return discountedPrice;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

}
