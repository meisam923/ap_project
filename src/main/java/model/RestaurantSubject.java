package model;


public interface RestaurantSubject {

    void registerObserver(RestaurantObserver o);

    void removeObserver(RestaurantObserver o);

    boolean requestConfirmation(Restaurant restaurant); // asks observers to confirm

}
