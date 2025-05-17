package model;

import exception.NotAcceptableException;

import java.util.ArrayList;

//singleton
public class RestaurantManager {
    private static RestaurantManager instance;
    private final ArrayList<Restaurant> restaurants = new ArrayList<>();
    private static RestaurantRegisterSystem restaurantRegisterSystem;

    public static RestaurantManager getInstance() {
        if (instance == null) {
            instance = new RestaurantManager();
        }
        restaurantRegisterSystem = RestaurantRegisterSystem.getInstance();
        return instance;
    }

    public void addRestaurant(Address address, Location location, String phone_number, String title, Owner owner, String category) throws NotAcceptableException {

        RestaurantRegisterSystem restaurantRegisterSystem;
        restaurantRegisterSystem = RestaurantRegisterSystem.getInstance();
        Restaurant new_restaurant = new Restaurant(address, location, phone_number, title, owner, category);
        restaurantRegisterSystem.requestConfirmation(new_restaurant);

    }

    public void addRestaurantObserver(RestaurantObserver o) {
        restaurantRegisterSystem.registerObserver(o);
    }
}
