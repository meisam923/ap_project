package model;

public enum FoodCategory {
    IRANIAN("Iranian"),
    FAST_FOOD("Fast Food"),
    PIZZA("Pizza"),
    BURGER("Burger"),
    SANDWICH("Sandwich"),
    KEBAB("Kebab"),
    SEAFOOD("Seafood"),
    VEGETARIAN("Vegetarian"),
    ASIAN("Asian"),
    ITALIAN("Italian"),
    DESSERT("Dessert"),
    BREAKFAST("Breakfast"),
    SALAD("Salad"),
    DRINK("Drink"),
    SNACK("Snack");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FoodCategory makeFoodCategory(String displayName) {
        for (FoodCategory foodCategory : FoodCategory.values()) {
            if (foodCategory.getDisplayName().equals(displayName)) {
                return foodCategory;
            }
        }
        return null;
    }
}