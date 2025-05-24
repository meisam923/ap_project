package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

//toman price class
@Embeddable
@Getter
@Setter
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


    public long getPriceWithoutDiscount() {
        return price;
    }


    public boolean hasDiscount() {
        return discount != null;
    }
    public void removeDiscount() {
        this.discount = null;
    }

}
@Getter
@Setter
@Embeddable
class Discount {

    @Column(name = "discount_percentage")
    private int percentage;

    public Discount() {
    }

    public Discount(int percentage) {
        this.percentage = percentage;
    }


}
