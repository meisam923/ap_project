package Services;

import model.Restaurant;
import observers.RestaurantObserver;

import java.util.ArrayList;

public class RestaurantRegisterSystem {
    private static RestaurantRegisterSystem instance;

    private ArrayList<RestaurantObserver> observers = new ArrayList<RestaurantObserver>();

    public static RestaurantRegisterSystem getInstance() {
        if (instance == null) {
            instance = new RestaurantRegisterSystem();
        }
        return instance;
    }

    public void registerObserver(RestaurantObserver o) {
        observers.add(o);
    }

    public void removeObserver(RestaurantObserver o) {
        observers.remove(o);
    }
    public void  requestConfirmation(Restaurant restaurant) {

        for (RestaurantObserver o : observers) {
            o.registerRestaurant(restaurant);
        }
        System.out.println("Restaurant " + restaurant + " register request was sent.");
    }

}

