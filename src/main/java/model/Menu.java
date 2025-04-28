package model;

import java.util.ArrayList;

public class Menu {
    ArrayList<Food> foods;
    public Menu() {
        foods=new ArrayList<>();
    }

    public boolean addFood(Food food) {
        foods.add(food);
        return true;
    }
}
