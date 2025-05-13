package model;


import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;

@Embeddable
public final class Address {
    private String addressDetails;
    private String addressTitle;

    public Address(String addressDetails, String addressTitle) {
        this.addressDetails = addressDetails;
        this.addressTitle = addressTitle;
    }

    protected Address() {

    }
}
