package Services;

import model.Restaurant;
import observers.RestaurantObserver;

import java.util.ArrayList;

public class RestaurantRegisterService {
    private static RestaurantRegisterService instance;

    private ArrayList<RestaurantObserver> observers = new ArrayList<RestaurantObserver>();

    public static RestaurantRegisterService getInstance() {
        if (instance == null) {
            instance = new RestaurantRegisterService();
        }
        return instance;
    }

    public void registerObserver(RestaurantObserver o) {
        observers.add(o);
    }

    public void removeObserver(RestaurantObserver o) {
        observers.remove(o);
    }
    public void  requestConfirmation(Long id) {

        for (RestaurantObserver o : observers) {
            o.registerRestaurant(id);
        }
        System.out.println("Restaurant with id : "+ id +" register request was sent.");
    }

}

