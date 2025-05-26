package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestaurantDto {
    public record RegisterRestaurantDto(
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("phone") String phone,
            @JsonProperty("logoBase64") String logaBase64,
            @JsonProperty("tax_fee") int tax_fee,
            @JsonProperty("additional") int additional_fee
    ){}
    public record RegisterReponseRestaurantDto(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("phone") String phone,
            @JsonProperty("logoBase64") String logaBase64,
            @JsonProperty("tax_fee") int tax_fee,
            @JsonProperty("additional") int additional_fee
    ){}

}
