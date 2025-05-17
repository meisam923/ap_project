package model;

import org.jetbrains.annotations.NotNull;

public interface RestaurantObserver {
    void registerRestaurant( @NotNull Restaurant restaurant);

}