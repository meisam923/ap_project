package model;

import enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("BUYER")
public class Customer extends User {

    @Embedded
    private Location location;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> ordersAssigned = new ArrayList<>();



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_favorite_restaurants",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();

    public Customer() {
        super();
        setRole(Role.BUYER);
    }

    public Customer(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber);
        setRole(Role.BUYER); 
    }

    public void addFavorite(Restaurant restaurant) {
        if (!this.favoriteRestaurants.contains(restaurant)) {
            this.favoriteRestaurants.add(restaurant);
        }
    }

    public boolean removeFavoriteById(int restaurantId) {
        return this.favoriteRestaurants.removeIf(restaurant -> restaurant.getId() == restaurantId);
    }
}