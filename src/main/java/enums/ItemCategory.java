package enums;

public enum ItemCategory {
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

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ItemCategory makeFoodCategory(String displayName) {
        for (ItemCategory foodCategory : ItemCategory.values()) {
            if (foodCategory.getDisplayName().equals(displayName)) {
                return foodCategory;
            }
        }
        return null;
    }
    public static ItemCategory buildCategory(String category) {
        switch (category) {
            case "Iranian":
                return ItemCategory.IRANIAN;
            case "Fast Food":
                return ItemCategory.FAST_FOOD;
            case "Pizza":
                return ItemCategory.PIZZA;
            case "Burger":
                return ItemCategory.BURGER;
            case "Sandwich":
                return ItemCategory.SANDWICH;
            case "Kebab":
                return ItemCategory.KEBAB;
            case "Seafood":
                return ItemCategory.SEAFOOD;
            case "Vegetarian":
                return ItemCategory.VEGETARIAN;
            default:
                return null;
        }
    }
}