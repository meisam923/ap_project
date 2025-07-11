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
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("image_url") String imageUrl
    ) {}

}
