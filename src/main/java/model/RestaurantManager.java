package model;



import java.util.ArrayList;

//singleton
public class RestaurantManager {
    private static RestaurantManager instance;

    private final ArrayList<Restaurant> restaurants = new ArrayList<>();

    public static RestaurantManager getInstance() {
        if (instance == null) {
            instance = new RestaurantManager();
        }
            return instance;
    }
    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
    }
}
