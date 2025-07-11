package Controller;

import dao.CustomerDao;
import dao.RestaurantDao;
import dto.RestaurantDto;
import enums.OperationalStatus;
import exception.NotFoundException;
import model.Customer;
import model.Restaurant;
import model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FavoriteController {

    private final CustomerDao customerDao = new CustomerDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();

    public List<RestaurantDto.RestaurantSchemaDTO> getFavorites(User user) {
        if (!(user instanceof Customer)) {
            throw new SecurityException("Forbidden: Only buyers can have favorites.");
        }

        Customer customerWithFavorites = customerDao.findByIdWithFavorites(user.getId());
        if (customerWithFavorites == null) {
            return Collections.emptyList();
        }

        List<Restaurant> favoriteEntities = customerWithFavorites.getFavoriteRestaurants();

        if (favoriteEntities == null || favoriteEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return favoriteEntities.stream()
                .map(this::mapRestaurantToSchemaDTO)
                .collect(Collectors.toList());
    }

    public void addFavorite(User user, int restaurantId) throws NotFoundException {
        if (!(user instanceof Customer)) {
            throw new SecurityException("Forbidden: Only buyers can add favorites.");
        }

        Customer customer = customerDao.findByIdWithFavorites(user.getId());
        if (customer == null) throw new NotFoundException(404, "Customer not found.");

        Restaurant restaurantToAdd = restaurantDao.findById((long) restaurantId);
        if (restaurantToAdd == null) {
            throw new NotFoundException(404, "Restaurant with ID " + restaurantId + " not found.");
        }

        customer.addFavorite(restaurantToAdd);
        customerDao.update(customer);
    }

    public void removeFavorite(User user, int restaurantId) {
        if (!(user instanceof Customer)) {
            throw new SecurityException("Forbidden: Only buyers can remove favorites.");
        }

        Customer customer = customerDao.findByIdWithFavorites(user.getId());
        if (customer == null) return;

        boolean removed = customer.removeFavoriteById(restaurantId);

        if (removed) {
            customerDao.update(customer);
        } else {
            System.out.println("Info: Attempted to remove a restaurant that was not in favorites.");
        }
    }

    private RestaurantDto.RestaurantSchemaDTO mapRestaurantToSchemaDTO(Restaurant restaurant) {
        if (restaurant == null) return null;

        boolean isOpen = restaurant.getOperationalStatus() == OperationalStatus.OPEN;
        String category = restaurant.getCategory() != null ? restaurant.getCategory().name() : "Uncategorized";

        return new RestaurantDto.RestaurantSchemaDTO(
                restaurant.getId(),
                restaurant.getTitle(),
                restaurant.getAddress(),
                category,
                restaurant.getAverageRating(),
                restaurant.getLogoBase64(),
                isOpen
        );
    }
}