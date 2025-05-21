package observers;

import model.Restaurant;
import org.jetbrains.annotations.NotNull;

public interface RestaurantObserver {
    void registerRestaurant( Restaurant restaurant);

}