package model;

import java.util.ArrayList;
import jakarta.persistence.*;

public class Menu {

    private ArrayList<Item> items = new ArrayList<>(); //initialized here constructor unknown

    public void addItem(Item item) {
        items.add(item);
    }
}

