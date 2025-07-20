package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class VendorDto {


    public record VendorListRequestDTO(
            @JsonProperty("search") String search,
            @JsonProperty("keywords") List<String> keywords,
            @JsonProperty("min_rating") Double minRating
    ) {}

    public record RestaurantSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("address") String address,
            @JsonProperty("category") String category,
            @JsonProperty("rating") Double rating,
            @JsonProperty("logoBase64") String logoUrl,
            @JsonProperty("is_open") Boolean isOpen,
            @JsonProperty("tax_fee") int tax_fee,
            @JsonProperty("additional_fee") int additional_fee,
            @JsonProperty("phone") String phone
    ) {}
    public record FoodItemSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("imageBase64") String imageUrl,
            @JsonProperty("description") String description,
            @JsonProperty("vendor_id") int vendorId,
            @JsonProperty("price") long price,
            @JsonProperty("supply") int supply,
            @JsonProperty("keywords")  List<String> keywords
    ) {}

    public record VendorMenuResponseDTO(
            @JsonProperty("vendor") RestaurantSchemaDTO vendor,
            @JsonProperty("menu_titles") List<String> menuTitles,
            @JsonProperty("menu_title") Map<String, List<FoodItemSchemaDTO>> menus
    ) {}
}