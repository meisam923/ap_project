package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.time.LocalDateTime;

//toman price class
@Embeddable
public class Price {
    private long price;

    @Embedded
    private Discount discount;

    public Price() {
    }

    public Price(long price) {
        this.price = price;
    }

    public static long sumPrice(Price p1, Price p2) {
        return p1.price + p2.price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setDiscount(int percentage, LocalDateTime expiration) {
        this.discount = new Discount(percentage, expiration);
    }

    public boolean isDiscountActive(LocalDateTime now) {
        return discount != null && discount.isActive(now);
    }

    public long getPriceWithoutDiscount() {
        return price;
    }

    public long getPriceWithDiscount(LocalDateTime now) {
        if (isDiscountActive(now)) {
            return (price * (100 - discount.getPercentage())) / 100;
        }
        return price;
    }
    public boolean hasDiscount() {
        return discount != null;
    }
    public void removeDiscount() {
        this.discount = null;
    }
    public long getDiscountedAmount(LocalDateTime now) {  //added this to show how much the customer is actually saving money
        if (isDiscountActive(now)) {
            return (price * discount.getPercentage()) / 100;
        }
        return 0;
    }

}

@Embeddable
class Discount {

    @Column(name = "discount_percentage")
    private int percentage;

    @Column(name = "discount_expiration")
    private LocalDateTime expiration;

    public Discount() {
        // Required by JPA
    }

    public Discount(int percentage, LocalDateTime expiration) {
        this.percentage = percentage;
        this.expiration = expiration;
    }

    public boolean isActive(LocalDateTime now) {
        return expiration != null && now.isBefore(expiration);
    }

    public int getPercentage() {
        return percentage;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }
}
