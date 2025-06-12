package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class VendorDto {


    public record VendorListRequestDTO(
            @JsonProperty("search") String search,
            @JsonProperty("keywords") List<String> keywords
    ) {}

    public record RestaurantSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("category") String category,
            @JsonProperty("rating") Double rating,
            @JsonProperty("logo_url") String logoUrl,
            @JsonProperty("is_open") Boolean isOpen
    ) {}

    public record FoodItemSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("image_url") String imageUrl
    ) {}

    public record VendorMenuResponseDTO(
            @JsonProperty("vendor") RestaurantSchemaDTO vendor,
            @JsonProperty("menu_titles") List<String> menuTitles,
            Map<String, List<FoodItemSchemaDTO>> menus
    ) {}
}