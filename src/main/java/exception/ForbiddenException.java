package exception;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ForbiddenException extends Exception {
    private final int status_code;
    public ForbiddenException(int status_code) {
        super("Forbidden request");
        this.status_code = status_code;
    }
}
