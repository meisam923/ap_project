package model;

import jakarta.persistence.Embeddable;

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

    public String getAddressDetails() {
        return addressDetails;
    }

    public String getAddressTitle() {
        return addressTitle;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", addressTitle, addressDetails);
    }
}
