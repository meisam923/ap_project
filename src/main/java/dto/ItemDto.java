package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class ItemDto {

    public record ItemListRequestDTO(
           @JsonProperty("search") String search,
           @JsonProperty("price") Integer price,
           @JsonProperty("keywords")List<String> keywords
    ) {}

    public record FoodItemSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("imageBase64") String imageUrl,
            @JsonProperty("description") String description,
            @JsonProperty("vendor_id") int vendorId,
            @JsonProperty("price") int price,
            @JsonProperty("supply") int supply,
            @JsonProperty("keywords") List<String> keywords
    ) {}

}
