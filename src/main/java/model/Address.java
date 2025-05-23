package model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public final class Address {
    private String addressDetails;
    private String addressTitle;

    public Address(String addressDetails, String addressTitle) {
        this.addressDetails = addressDetails;
        this.addressTitle = addressTitle;
    }

    public Address() {
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", addressTitle, addressDetails);
    }
}
