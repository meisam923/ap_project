package dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A container class for all DTOs related to Orders.
 */
public class OrderDto {

    /**
     * Represents a single item within a new order request.
     */
    public static record SubmitOrderItemDTO(
            @JsonProperty("item_id") Integer itemId,
            @JsonProperty("quantity") Integer quantity
    ) {}

    /**
     * DTO for the request body of POST /orders.
     */
    public static record SubmitOrderRequestDTO(
            @JsonProperty("delivery_address") String deliveryAddress,
            @JsonProperty("vendor_id") Integer vendorId,
            @JsonProperty("coupon_id") Integer couponId, // Optional
            @JsonProperty("items") List<SubmitOrderItemDTO> items
    ) {}

    /**
     * DTO representing a complete order in an API response.
     * Corresponds to #/components/schemas/order
     */
    public static record OrderSchemaDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("delivery_address") String deliveryAddress,
            @JsonProperty("customer_id") Long customerId,
            @JsonProperty("vendor_id") int vendorId,
            @JsonProperty("coupon_id") Integer couponId,
            @JsonProperty("item_ids") List<Integer> itemIds,
            @JsonProperty("raw_price") BigDecimal rawPrice,
            @JsonProperty("tax_fee") BigDecimal taxFee,
            @JsonProperty("courier_fee") BigDecimal courierFee,
            @JsonProperty("additional_fee") BigDecimal additionalFee,
            @JsonProperty("pay_price") BigDecimal payPrice,
            @JsonProperty("courier_id") Long courierId,
            @JsonProperty("status") String status,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("updated_at") LocalDateTime updatedAt
    ) {}
}