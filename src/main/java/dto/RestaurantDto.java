package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class RestaurantDto {
    public record RegisterRestaurantDto(
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("phone") String phone,
            @JsonProperty("logoBase64") String logaBase64,
            @JsonProperty("tax_fee") int tax_fee,
            @JsonProperty("additional_fee") int additional_fee
    ){}
    public record RegisterReponseRestaurantDto(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("phone") String phone,
            @JsonProperty("logoBase64") String logaBase64,
            @JsonProperty("tax_fee") int tax_fee,
            @JsonProperty("additional_fee") int additional_fee
    ){}
    public record AddItemToRestaurantDto(
            @JsonProperty("name") String name,
            @JsonProperty("imageBase64") String imageBase64,
            @JsonProperty("description") String description,
            @JsonProperty("price") int price,
            @JsonProperty("supply") int supply,
            @JsonProperty("keywords") ArrayList<String> keywords,
            @JsonProperty("category") String category
    ){}
    public record AddItemToRestaurantResponseDto(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("imageBase64") String imageBase64,
            @JsonProperty("description") String description,
            @JsonProperty("vendor_id") int vendor_id,
            @JsonProperty("price") int price,
            @JsonProperty("supply") int supply,
            @JsonProperty("keywords") ArrayList<String> keywords
    ){}

}
/*
{
  "name": "string",
  "imageBase64": "string",
  "description": "string",
  "price": 0,
  "supply": 0,
  "keywords": [
    "string"
  ]
}*/
