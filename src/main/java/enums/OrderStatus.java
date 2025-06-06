package enums;

import exception.InvalidInputException;

public enum OrderStatus {
    SUBMITTED,
    UNPAID_AND_CANCELLED,
    WAITING_VENDOR,
    CANCELLED,
    FINDING_COURIER,
    ON_THE_WAY,
    COMPLETED;

    /**
     * Converts a string (potentially with spaces and different casing)
     * to an OrderStatus enum constant. It normalizes the input by
     * converting to uppercase and replacing spaces with underscores.
     *
     * @param text The input string to convert.
     * @return The matching OrderStatus.
     * @throws IllegalArgumentException if the text does not match any OrderStatus
     * after normalization.
     */
    public static OrderStatus fromString(String text) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException(400, "Invalid status");
        }

        // Normalize to uppercase and replace spaces with underscores
        String normalizedText = text.trim().toUpperCase().replace(' ', '_');

        try {
            return OrderStatus.valueOf(normalizedText);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InvalidInputException(400, "Invalid status");
        }
    }
}