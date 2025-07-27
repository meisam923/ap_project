package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {
    public record UpdateUserStatusRequestDTO(
            @JsonProperty("status") String status
    ){}
    public record UserSchemaDTO(
            @JsonProperty("id") long id,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("role") String role,
            @JsonProperty("address") String address,
            @JsonProperty("profileImageBase64") String profileImageBase64,
            @JsonProperty("status") String status,
            @JsonProperty("bank_info") UserDto.RegisterRequestDTO.BankInfoDTO bankInfo
    ) {}
    public static record OrderSchemaDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("delivery_address") String deliveryAddress,
            @JsonProperty("customer_id") Long customerId,
            @JsonProperty("customer_name") String customerName,
            @JsonProperty("vendor_id") int vendorId,
            @JsonProperty("vendor_name") String vendorName,
            @JsonProperty("coupon_id") Integer couponId,
            @JsonProperty("items") List<OrderItemDto> items,
            @JsonProperty("raw_price") BigDecimal rawPrice,
            @JsonProperty("tax_fee") BigDecimal taxFee,
            @JsonProperty("courier_fee") BigDecimal courierFee,
            @JsonProperty("additional_fee") BigDecimal additionalFee,
            @JsonProperty("pay_price") BigDecimal payPrice,
            @JsonProperty("courier_id") Long courierId,
            @JsonProperty("status") String status,
            @JsonProperty("restaurantStatus")  String restaurantStatus,
            @JsonProperty("delivery_Status")  String deliveryStatus,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("updated_at") String updatedAt
    ) {}
    public record OrderItemDto(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("pricePerItem") BigDecimal pricePerItem,
            @JsonProperty("totalPriceForItem") BigDecimal totalPriceForItem,
            @JsonProperty("quantity") int quantity
    ){}

    public record TransactionSchemaDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("order_id") Long orderId,
            @JsonProperty("user_id") Long userId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("method") String method,
            @JsonProperty("status") String status,
            @JsonProperty("type") String type,
            @JsonProperty("created_at") String createdAt
    ) {}
    public record CouponSchemaDTO(
            @JsonProperty("id") int id,
            @JsonProperty("coupon_code") String couponCode,
            @JsonProperty("type") String type,
            @JsonProperty("value") BigDecimal value,
            @JsonProperty("min_price") Integer minPrice,
            @JsonProperty("user_count") Integer userCount,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate
    ) {}
    public static record UpdateUserStatusResponseDTO(
            @JsonProperty("message") String message,
            @JsonProperty("user_id") String userId,
            @JsonProperty("new_status") String newStatus
    ) {}
}
