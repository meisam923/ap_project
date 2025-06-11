package exception;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ConflictException extends Exception {
    private final int status_code;
    public ConflictException(int status_code) {
        super("Conflict occured");
        this.status_code = status_code;
    }
}
