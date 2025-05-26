package exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final int status_code;

    public UserNotFoundException(int status_code, String message) {
        super(message);
        this.status_code = status_code;
    }
}
