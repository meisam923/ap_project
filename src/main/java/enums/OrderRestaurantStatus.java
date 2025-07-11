package enums;

import exception.InvalidInputException;

public enum OrderRestaurantStatus {
    BASE,
    ACCEPTED,
    REJECTED,
    SERVED;

    public static OrderRestaurantStatus fromString(String text) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(400, "Invalid status");
        }

        // Normalize to uppercase and replace spaces with underscores
        String normalizedText = text.trim().toUpperCase().replace(' ', '_');

        try {
            return OrderRestaurantStatus.valueOf(normalizedText);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidInputException(400, "restaurant status");
        }
    }

    public boolean equals(OrderRestaurantStatus other) {
        return this.ordinal() == other.ordinal();
    }
}