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

        if (dto.rating() != null) review.setRating(dto.rating());
        if (dto.comment() != null) review.setComment(dto.comment());
        if (dto.imageBase64() != null) review.setImagesBase64(dto.imageBase64());

        ratingDao.update(review);
        updateRestaurantAverageRating(review.getOrder().getRestaurant());

        return mapReviewToDto(review);
    }

    public void deleteRating(Long ratingId, User user) throws Exception {
        Review review = ratingDao.findById(ratingId);
        if (review == null) {
            throw new NotFoundException("Rating with ID " + ratingId + " not found.");
        }
        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Forbidden: You can only delete your own reviews.");
        }

        Restaurant restaurantToUpdate = review.getOrder().getRestaurant();
        ratingDao.deleteById(ratingId);
        updateRestaurantAverageRating(restaurantToUpdate);
    }

    private void updateRestaurantAverageRating(Restaurant restaurant) throws Exception {
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