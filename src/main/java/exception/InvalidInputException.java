package exception;

public class InvalidInputException extends Exception {
    private final int statusCode;

    public InvalidInputException(int statusCode, String field) {
        super("Invalid "+field);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

