package Controller;

import dao.OrderDao;
import dao.RatingDao;
import dao.RestaurantDao;
import dto.RatingDto;
import enums.OrderStatus;
import model.Order;
import model.Restaurant;
import model.Review;
import model.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RatingController {

    private final RatingDao ratingDao = new RatingDao();
    private final OrderDao orderDao = new OrderDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();

    public RatingDto.RatingSchemaDTO submitRating(RatingDto.SubmitRatingRequestDTO ratingDto, User user) throws Exception {
        Order order = orderDao.findById(ratingDto.orderId());
        if (order == null) {
            throw new NotFoundException("Order with ID " + ratingDto.orderId() + " not found.");
        }
        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new SecurityException("Forbidden: You can only review your own orders.");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Invalid Input: Order must be completed to be reviewed.");
        }
        if (ratingDao.findByOrderId(order.getId()).isPresent()) {
            throw new ConflictException("Conflict: This order has already been reviewed.");
        }

        Review newReview = new Review(order, user, ratingDto.rating(), ratingDto.comment());
        if (ratingDto.imageBase64() != null) {
            newReview.setImagesBase64(ratingDto.imageBase64());
        }
        ratingDao.save(newReview);

        updateRestaurantAverageRating(order.getRestaurant());

        return mapReviewToDto(newReview);
    }

    public RatingDto.ItemRatingsResponseDTO getRatingsForItem(Integer itemId) {
        List<Review> reviews = ratingDao.findReviewsByItemId(itemId);

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        List<RatingDto.RatingSchemaDTO> comments = reviews.stream()
                .map(this::mapReviewToDto)
                .collect(Collectors.toList());

        return new RatingDto.ItemRatingsResponseDTO(avgRating, comments);
    }

    public Optional<RatingDto.RatingSchemaDTO> getRatingById(Long ratingId) {
        Review review = ratingDao.findById(ratingId);
        return Optional.ofNullable(mapReviewToDto(review));
    }

    public RatingDto.RatingSchemaDTO updateRating(Long ratingId, RatingDto.UpdateRatingRequestDTO dto, User user) throws Exception {
        Review review = ratingDao.findById(ratingId);
        if (review == null) {
            throw new NotFoundException("Rating with ID " + ratingId + " not found.");
        }
        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Forbidden: You can only update your own reviews.");
        }

        Long restaurantIdToUpdate = null;
        // Ensure Order and Restaurant proxies are initialized or their IDs are fetched
        // while the session that loaded 'review' is potentially still active.
        if (review.getOrder() != null && review.getOrder().getRestaurant() != null) {
            // Option 1: Initialize the proxy if you need the full Restaurant object later
            // and the session is still active here.
            // org.hibernate.Hibernate.initialize(review.getOrder().getRestaurant());
            // restaurantToUpdate = review.getOrder().getRestaurant();

            // Option 2 (Recommended for updateRestaurantAverageRating): Get the ID.
            restaurantIdToUpdate = (long) review.getOrder().getRestaurant().getId();
        } else {
            // This case should ideally not happen if data integrity is maintained.
            // If it does, it points to an issue with how the review was loaded or associated.
            throw new IllegalStateException("Review with ID " + ratingId + " does not have a valid associated order or restaurant.");
        }

        if (dto.rating() != null) review.setRating(dto.rating());
        if (dto.comment() != null) review.setComment(dto.comment());
        // Assuming your RatingDto.UpdateRatingRequestDTO now has imageBase64
        // If not, this line would cause a compile error or should be removed.
        if (dto.imageBase64() != null) review.setImagesBase64(dto.imageBase64());

        ratingDao.update(review);

        if (restaurantIdToUpdate != null) {
            // Assuming updateRestaurantAverageRating now accepts a Long restaurantId
            updateRestaurantAverageRating(restaurantDao.findById(restaurantIdToUpdate));
        } else {
            // Log or handle the case where restaurantId could not be determined,
            // though the earlier exception should prevent this.
            System.err.println("Could not update average rating as restaurant ID was not found for review: " + ratingId);
        }

        // Ensure mapReviewToDto either works with a detached 'review' (only accessing eagerly loaded fields or IDs)
        // or is called within an active session context.
        // If 'review' becomes detached and mapReviewToDto tries to access lazy fields, it will fail.
        // One way to ensure this is to re-fetch 'review' by ID before mapping if necessary,
        // or make sure the entire method is transactional.
        return mapReviewToDto(ratingDao.findById(ratingId)); // Re-fetch for DTO mapping if session might have closed
    }

    public void deleteRating(Long ratingId, User user) throws Exception {
        Review review = ratingDao.findById(ratingId);
        if (review == null) {
            throw new NotFoundException("Rating with ID " + ratingId + " not found.");
        }
        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Forbidden: You can only delete your own reviews.");
        }

        // Get the ID of the restaurant BEFORE deleting the review.
        // This ensures that even if 'review' or its associations become invalid after deletion,
        // you still have the necessary ID.
        Long restaurantIdToUpdate = null;
        if (review.getOrder() != null && review.getOrder().getRestaurant() != null) {
            restaurantIdToUpdate = (long) review.getOrder().getRestaurant().getId();
        } else {
            // This indicates a data integrity problem or an issue with how 'review' was loaded.
            // A review should always be linked to an order, and an order to a restaurant.
            throw new IllegalStateException("Review with ID " + ratingId + " is not properly associated with an order or restaurant, cannot determine restaurant for average rating update.");
        }

        ratingDao.deleteById(ratingId); // Delete the review

        // Now call updateRestaurantAverageRating with the ID obtained earlier.
        // The updateRestaurantAverageRating method should be responsible for fetching
        // the Restaurant entity by this ID within its own session/transaction.
        if (restaurantIdToUpdate != null) {
            updateRestaurantAverageRating(restaurantDao.findById(restaurantIdToUpdate));
        } else {
            // This path should ideally not be reached if the IllegalStateException above is thrown.
            // Log an error if, for some reason, restaurantIdToUpdate is null here.
            System.err.println("Could not update average rating as restaurant ID was not determined before deleting review: " + ratingId);
        }
    }

    private void updateRestaurantAverageRating(Restaurant restaurant) throws Exception {
        if (restaurant == null) {
            return;
        }
        List<Review> reviews = ratingDao.findAllByRestaurantId(restaurant.getId());

        double newAverage = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        BigDecimal roundedAverage = BigDecimal.valueOf(newAverage).setScale(1, RoundingMode.HALF_UP);

        restaurant.setAverageRating(roundedAverage.doubleValue());
        restaurantDao.update(restaurant);
        System.out.println("Updated average rating for restaurant ID " + restaurant.getId() + " to " + roundedAverage);
    }

    private RatingDto.RatingSchemaDTO mapReviewToDto(Review review) {
        if (review == null) return null;
        return new RatingDto.RatingSchemaDTO(
                review.getId(),
                review.getOrder().getId(),
                review.getAuthor().getId(),
                review.getRating(),
                review.getComment(),
                review.getImagesBase64(),
                review.getCreatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException { public NotFoundException(String message) { super(message); } }
    public static class ConflictException extends RuntimeException { public ConflictException(String message) { super(message); } }
}