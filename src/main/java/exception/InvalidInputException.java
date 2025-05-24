package exception;

import lombok.Getter;

@Getter
public class InvalidInputException extends Exception {
    private final int statusCode;

    public InvalidInputException(int statusCode, String field) {
        super("Invalid "+field);
        this.statusCode = statusCode;
    }

}

