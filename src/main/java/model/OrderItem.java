package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerItem;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPriceForItem;

    public OrderItem(Order order, Integer itemId, String itemName, Integer quantity, BigDecimal pricePerItem) {
        this.order = order;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.totalPriceForItem = pricePerItem.multiply(BigDecimal.valueOf(quantity));
    }
}