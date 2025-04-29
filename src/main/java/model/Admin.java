package model;

public class Admin implements RestaurantObserver {
    Role role = Role.ADMIN;

    public boolean registerRestaurant(Restaurant restaurant) {
        return false;
    }

}
