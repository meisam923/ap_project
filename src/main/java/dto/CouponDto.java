package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CouponDto {

    public static record CouponSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("coupon_code") String couponCode,
            @JsonProperty("type") String type,
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("min_price") Integer minPrice,
            @JsonProperty("user_count") Integer userCount,
            @JsonProperty("start_date") LocalDate startDate,
            @JsonProperty("end_date") LocalDate endDate
    ) {}

    public static record CouponInputSchemaDTO(
            @JsonProperty("coupon_code") String couponCode,
            @JsonProperty("type") String type,
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("min_price") Integer minPrice,
            @JsonProperty("user_count") Integer userCount,
            @JsonProperty("start_date") LocalDate startDate,
            @JsonProperty("end_date") LocalDate endDate
    ) {}


}