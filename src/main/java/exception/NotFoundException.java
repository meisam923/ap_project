package exception;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class NotFoundException extends Exception {
    private final int status_code;
    public NotFoundException(int status_code, String message) {
        super(message+" Not Found");
        this.status_code = status_code;
    }
}
