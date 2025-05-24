package exception;

import lombok.Getter;

@Getter
public class InvalidInputException extends Exception {
    private final int status_code;
    public InvalidInputException(int statusCode, String field) {
        super("Invalid "+field);
        this.status_code= statusCode;
    }

}

