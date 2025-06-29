package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

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
    public record OrderResponseDto(
            @JsonProperty("id") int id,
            @JsonProperty("delivery_address") String delivery_address,
            @JsonProperty("customer_id") int customer_id,
            @JsonProperty("vendor_id") int vendor_id,
            @JsonProperty("coupon_id") Integer coupon_id,
            @JsonProperty("item_ids") List<Integer> item_ids,
            @JsonProperty("raw_price") java.math.BigDecimal raw_price,
            @JsonProperty("tax_fee") java.math.BigDecimal tax_fee,
            @JsonProperty("additional_fee") java.math.BigDecimal additional_fee,
            @JsonProperty("courier_fee") java.math.BigDecimal courier_fee,
            @JsonProperty("pay_price") java.math.BigDecimal pay_price,
            @JsonProperty("courier_id") Integer courier_id,
            @JsonProperty("status") String status,
            @JsonProperty("created_at") String created_at,
            @JsonProperty("updated_at") String updated_at
    ) {}

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
