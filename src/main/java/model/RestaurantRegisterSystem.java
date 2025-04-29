package model;

import java.util.ArrayList;

//singeleton
public class RestaurantRegisterSystem implements RestaurantSubject {
    private static RestaurantRegisterSystem instance;

    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

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
    public boolean requestConfirmation(Restaurant restaurant) {
        boolean result = false;
        for (RestaurantObserver o : observers) {
            result = o.registerRestaurant(restaurant);
        }
        if (result) {
            System.out.println("Successfully registered Restaurant " + restaurant);
            result = true;
        }

        System.out.println("Failed to register Restaurant " + restaurant);
        return result;


    }
    public Restaurant register(Address address,
                         Location location,
                         String phone_number,
                         String title,
                         Owner owner,
                         ArrayList<Period> working_periods,
                         Menu menu)
    {
        //add some conditional check

        Restaurant restaurant = new Restaurant(address, location, phone_number, title, owner, working_periods, menu);
        return restaurant;
    }



}
