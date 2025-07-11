package exception;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DeliveryAssigendException extends Exception {
    private final int status_code;
    public DeliveryAssigendException(int status_code) {
        super("Delivery already assigned");
        this.status_code = status_code;
    }
}
