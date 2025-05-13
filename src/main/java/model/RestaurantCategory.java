package model;

import org.jetbrains.annotations.NotNull;
import jakarta.persistence.*;

public enum RestaurantCategory {
    FAST_FOOD, CAFE, TRADITIONAL,BREAKFAST,SEAFOOD,ITALIAN;//TBD

    public static RestaurantCategory buildCategory(String category) {
        switch (category) {
            case "Fast Food":
                return FAST_FOOD;
            case "Cafe":
                return CAFE;
            case "Traditional":
                return TRADITIONAL;
            case "Breakfast":
                return BREAKFAST;
            case "SeaFood":
                return SEAFOOD;
            case "Italian":
                return ITALIAN;
                default:
                    return null;
        }
    }
}

