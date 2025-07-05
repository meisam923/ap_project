package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RatingDto {

    public static record SubmitRatingRequestDTO(
            @JsonProperty("order_id") Long orderId,
            @JsonProperty("rating") Integer rating,
            @JsonProperty("comment") String comment,
            @JsonProperty("imageBase64") List<String> imageBase64
    ) {}

    public static record UpdateRatingRequestDTO(
            @JsonProperty("rating") Integer rating,
            @JsonProperty("comment") String comment,
            @JsonProperty("imageBase64") List<String> imageBase64
    ) {}

    public static record RatingSchemaDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("order_id") Long orderId,
            @JsonProperty("user_id") Long userId,
            @JsonProperty("rating") Integer rating,
            @JsonProperty("comment") String comment,
            @JsonProperty("image_base64") List<String> imageBase64,
            @JsonProperty("created_at") LocalDateTime createdAt
    ) {}

    public static record ItemRatingsResponseDTO(
            @JsonProperty("avg_rating") Double avgRating,
            @JsonProperty("comments") List<RatingSchemaDTO> comments
    ) {}
}