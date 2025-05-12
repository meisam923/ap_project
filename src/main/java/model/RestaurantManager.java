

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
    public void addRestaurant (Address address, Location location, String phone_number, String title, Owner owner, ArrayList<Period> working_periods) {

        RestaurantRegisterSystem restaurantRegisterSystem;
        restaurantRegisterSystem = RestaurantRegisterSystem.getInstance();
        Restaurant new_restaurant = new Restaurant(address, location, phone_number, title, owner);
        if (restaurantRegisterSystem.requestConfirmation(new_restaurant))
            restaurants.add(new_restaurant);
        else
            System.out.println("Failed to register Restaurant try again later" + new_restaurant);

    }
}
