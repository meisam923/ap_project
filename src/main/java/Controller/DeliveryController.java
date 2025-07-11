package Controller;

import dao.OrderDao;
import dto.OrderDto;
import enums.OrderStatus;
import enums.Role;
import model.Deliveryman;
import model.Order;
import model.OrderItem;
import model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeliveryController {

    private final OrderDao orderDao = new OrderDao();

    public List<OrderDto.OrderSchemaDTO> getAvailableDeliveries(User courier) {
        if (courier.getRole() != Role.COURIER) {
            throw new SecurityException("Forbidden: Only couriers can view available deliveries.");
        }
        List<Order> availableOrders = orderDao.findAvailableForDelivery();
        return availableOrders.stream()
                .map(this::mapOrderToSchemaDTO)
                .collect(Collectors.toList());
    }

    public OrderDto.OrderSchemaDTO updateDeliveryStatus(Long orderId, String newStatusAction, User courier) {
        if (!(courier instanceof Deliveryman)) {
            throw new SecurityException("Forbidden: Only couriers can update delivery status.");
        }
        Deliveryman deliveryman = (Deliveryman) courier;

        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new NotFoundException("Order with ID " + orderId + " not found.");
        }

        String action = newStatusAction.toLowerCase().trim();

        switch (action) {
            case "accepted":
                if (order.getStatus() != OrderStatus.FINDING_COURIER) {
                    throw new ConflictException("Order is not available for acceptance.");
                }
                if (order.getDeliveryman() != null) {
                    throw new ConflictException("Delivery already assigned to another courier.");
                }
                order.setDeliveryman(deliveryman);
                break;

            case "received":
                if (order.getDeliveryman() == null || !order.getDeliveryman().getId().equals(deliveryman.getId())) {
                    throw new SecurityException("Forbidden: You are not the courier for this order.");
                }
                if (order.getStatus() != OrderStatus.ON_THE_WAY) {
                    throw new ConflictException("Order is not ready for pickup or has passed this stage.");
                }
                System.out.println("Courier has received the order from the restaurant.");
                break;

            case "delivered":
                if (order.getDeliveryman() == null || !order.getDeliveryman().getId().equals(deliveryman.getId())) {
                    throw new SecurityException("Forbidden: You are not the courier for this order.");
                }
                if (order.getStatus() != OrderStatus.ON_THE_WAY) {
                    throw new ConflictException("Order cannot be marked as delivered from its current state.");
                }
                order.setStatus(OrderStatus.COMPLETED);
                break;

            default:
                throw new InvalidInputException("Invalid status action: '" + newStatusAction + "'. Must be one of: accepted, received, delivered.");
        }
        order.updateStatus();
        orderDao.update(order);
        return mapOrderToSchemaDTO(order);
    }

    public List<OrderDto.OrderSchemaDTO> getDeliveryHistory(User courier, String searchFilter, String vendorFilter) {
        if (courier.getRole() != Role.COURIER) {
            throw new SecurityException("Forbidden: Only couriers can view delivery history.");
        }
        List<Order> orders = orderDao.findHistoryForCourier(courier.getId(), searchFilter, vendorFilter);
        return orders.stream()
                .map(this::mapOrderToSchemaDTO)
                .collect(Collectors.toList());
    }

    private OrderDto.OrderSchemaDTO mapOrderToSchemaDTO(Order order) {
        if (order == null) return null;
        List<Integer> itemIds = order.getItems().stream().map(OrderItem::getItemId).collect(Collectors.toList());
        return new OrderDto.OrderSchemaDTO(
                order.getId(), order.getDeliveryAddress(), order.getCustomer().getId(),
                order.getRestaurant().getId(), (order.getCoupon() != null) ? order.getCoupon().getId() : null,
                itemIds, order.getSubtotalPrice(), order.getTaxFee(), order.getDeliveryFee(),
                order.getAdditionalFee(), order.getTotalPrice(),
                (order.getDeliveryman() != null) ? order.getDeliveryman().getId() : null,
                order.getStatus().name(), order.getCreatedAt(), order.getUpdatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException { public NotFoundException(String message) { super(message); } }
    public static class ConflictException extends RuntimeException { public ConflictException(String message) { super(message); } }
    public static class InvalidInputException extends RuntimeException { public InvalidInputException(String message) { super(message); } }
}