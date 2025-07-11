package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryDto {

    public static record UpdateDeliveryStatusRequestDTO(
            @JsonProperty("status") String status
    ) {}
}