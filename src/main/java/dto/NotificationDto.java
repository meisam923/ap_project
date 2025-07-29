package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NotificationDto {
    public static record NotificationSchemaDTO(
            @JsonProperty("id") String id,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("is_read") boolean isRead
    ) {}
}