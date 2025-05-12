package model;

import java.time.LocalDateTime;

//toman price class
public class Price {
    private long price;
    private Discount discount;
    private boolean discountIsActive=false;
    public Price(long price) {
        this.price = price;
    }
    public static long sumPrice(Price temp_price1,Price temp_price2 ) {
        return temp_price1.price + temp_price2.price;
    }
    public void setPrice(long price) {
        this.price = price;
    }
    public void setDiscount(int percentage , LocalDateTime expiration) {
        this.discount = new Discount(percentage, expiration);
        this.discountIsActive=true;
    }
    public boolean DiscountIsActive(LocalDateTime now) {
        if (discount.isActive(now)) {
            discountIsActive = true;
            return true;
        }
        else{
            discount=null;
            discountIsActive=false;
            return false;}
    }
    public long getPriceWithoutDiscount() {
        return price;
    }
    public long getPriceWithDiscount(LocalDateTime now) {
        if (this.DiscountIsActive(now)) {
            return (100-discount.getPercentage())*price;
        }
        return price;
    }
}
