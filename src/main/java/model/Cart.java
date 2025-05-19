package model;

import exception.NotAcceptableException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private List<Item> items = new ArrayList<>();

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "coupon_percentage")
    private Integer couponPercentage;

    public Cart() {
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void applyCoupon(String code, int percentage) throws NotAcceptableException {
        if (code == null || code.isBlank() || percentage <= 0 || percentage > 100) {
            throw new NotAcceptableException("Invalid coupon");
        }
        this.couponCode = code;
        this.couponPercentage = percentage;
    }

    public void clearCoupon() {
        this.couponCode = null;
        this.couponPercentage = null;
    }

    public long getSubtotal() {
        long sum = 0;
        for (Item item : items) {
            sum += item.getPrice().getPriceWithoutDiscount();
        }
        return sum;
    }

    public long getAfterItemDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        long sum = 0;
        for (Item item : items) {
            sum += item.getPrice().getPriceWithDiscount(now);
        }
        return sum;
    }


    public long getItemDiscountSavings() {
        return getSubtotal() - getAfterItemDiscounts();
    }

    public long getCouponDiscountAmount() {
        if (couponPercentage == null || couponPercentage <= 0) {
            return 0;
        }
        long base = getAfterItemDiscounts();
        return (base * couponPercentage) / 100;
    }

    public long getTotal() {
        return getAfterItemDiscounts() - getCouponDiscountAmount();
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public String getCouponCode() {
        return couponCode;
    }

    public Integer getCouponPercentage() {
        return couponPercentage;
    }
}
