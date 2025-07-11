package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    public static record TopUpRequestDTO(
            @JsonProperty("amount") BigDecimal amount
    ) {}

    public static record PaymentRequestDTO(
            @JsonProperty("order_id") Long orderId,
            @JsonProperty("method") String method
    ) {}

    //  #/components/schemas/transaction
    public static record TransactionSchemaDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("order_id") Long orderId,
            @JsonProperty("user_id") Long userId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("method") String method,
            @JsonProperty("status") String status,
            @JsonProperty("type") String type,
            @JsonProperty("created_at") LocalDateTime createdAt
    ) {}
}