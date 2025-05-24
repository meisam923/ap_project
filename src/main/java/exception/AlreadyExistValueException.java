package exception;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AlreadyExistValueException extends Exception {
    private final int status_code;
    public AlreadyExistValueException(int status_code,String message) {
        super(message+" already exists");
        this.status_code = status_code;
    }
}
