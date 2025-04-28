package model;

import java.util.ArrayList;

public class Menu {
<<<<<<< HEAD
    ArrayList<Food> foods;
    public Menu() {
        foods=new ArrayList<>();
    }

    public boolean addFood(Food food) {
        foods.add(food);
        return true;
    }
=======
    private final ArrayList<MenuItem> menuItems = new ArrayList<>(); //initialized here constructor unknown


>>>>>>> f4c67e09cd4d7d8ee01083f0c66dba1698071eea
}
