package model;
//toman price class
public class Price {
    private long price;
    public Price(long price) {
        this.price = price;
    }
    public void sumPrice(Price temp_price) {
        this.price += temp_price.price;
    }

}
