package enums;

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
    public static OrderStatus fromString(String text) throws  IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input status string cannot be null or empty");
        }

        // Normalize to uppercase and replace spaces with underscores
        String normalizedText = text.trim().toUpperCase().replace(' ', '_');

        try {
            return OrderStatus.valueOf(normalizedText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "No OrderStatus found for input: '" + text + "' (normalized to: '" + normalizedText + "'). " +
                            "Valid values (with spaces replaced by underscores and in uppercase) are: " +
                            java.util.Arrays.stream(values()).map(Enum::name).collect(java.util.stream.Collectors.joining(", ")), e);
        }
    }

    /**
     * A lenient version of fromString that returns null instead of throwing an exception
     * if no match is found. Normalizes to uppercase and replaces spaces with underscores.
     *
     * @param text The input string to convert.
     * @return The matching OrderStatus, or null if no match.
     */
    public static OrderStatus fromStringLenient(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            String normalizedText = text.trim().toUpperCase().replace(' ', '_');
            return OrderStatus.valueOf(normalizedText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }
}