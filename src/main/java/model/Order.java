package model;

import enums.CouponType;
import enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotalPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal additionalFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryman_id")
    private Deliveryman deliveryman;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Review review;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Order() {
        this.status = OrderStatus.SUBMITTED;
    }

    public Order(Customer customer, Restaurant restaurant, String deliveryAddress) {
        this();
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress;
    }

    public void addOrderItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    public void calculateTotals() {
        this.subtotalPrice = items.stream()
                .map(OrderItem::getTotalPriceForItem)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (this.coupon != null) {
            if (this.coupon.getType() == CouponType.PERCENT) {
                BigDecimal discountPercentage = this.coupon.getValue().divide(new BigDecimal("100"));
                this.discountAmount = this.subtotalPrice.multiply(discountPercentage);
            } else {
                this.discountAmount = this.coupon.getValue();
            }
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        if (this.deliveryFee == null) this.deliveryFee = BigDecimal.ZERO;
        if (this.taxFee == null) this.taxFee = BigDecimal.ZERO;
        if (this.additionalFee == null) this.additionalFee = BigDecimal.ZERO;

        this.totalPrice = this.subtotalPrice
                .add(this.deliveryFee)
                .add(this.taxFee)
                .add(this.additionalFee)
                .subtract(this.discountAmount);

        if (this.totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            this.totalPrice = BigDecimal.ZERO;
        }

        this.totalPrice = this.totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
}