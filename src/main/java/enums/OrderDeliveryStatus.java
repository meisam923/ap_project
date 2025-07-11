package enums;

import exception.InvalidInputException;

public enum OrderDeliveryStatus {
    BASE,
    ACCEPTED,
    RECEIVED,
    DELIVERED,;

    public static OrderDeliveryStatus fromString(String text) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(400, "Invalid status");
        }

        // Normalize to uppercase and replace spaces with underscores
        String normalizedText = text.trim().toUpperCase().replace(' ', '_');

        try {
            return OrderDeliveryStatus.valueOf(normalizedText);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidInputException(400, "delivery status");
        }
    }

    public boolean equals(OrderDeliveryStatus other) {
        return this.ordinal() == other.ordinal();
    }
}