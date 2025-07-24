package Controller;

import dao.CouponDao;
import dao.ItemDao;
import dao.OrderDao;
import dao.RestaurantDao;
import dto.OrderDto;
import enums.OperationalStatus;
import enums.Role;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderController {

    private final OrderDao orderDao = new OrderDao();
    private final RestaurantDao restaurantDao = new RestaurantDao();
    private final ItemDao itemDao = new ItemDao();
    private final CouponDao couponDao = new CouponDao();

    public Optional<OrderDto.OrderSchemaDTO> submitOrder(OrderDto.SubmitOrderRequestDTO orderDto, User user) throws Exception {
        System.out.println("\n[DEBUG] --- Entering submitOrder method ---");

        if (!(user instanceof Customer)) {
            System.out.println("[DEBUG] ERROR: User is not a Customer.");
            throw new SecurityException("Forbidden: Only buyers can submit orders.");
        }
        Customer customer = (Customer) user;
        System.out.println("[DEBUG] User is a valid Customer. ID: " + customer.getId());

        // --- VALIDATION ---
        if (orderDto.deliveryAddress() == null || orderDto.deliveryAddress().isBlank()) {
            System.out.println("[DEBUG] ERROR: Delivery address is empty.");
            throw new IllegalArgumentException("Delivery address cannot be empty.");
        }
        System.out.println("[DEBUG] Delivery address is present.");

        if (orderDto.items() == null || orderDto.items().isEmpty()) {
            System.out.println("[DEBUG] ERROR: Items list is empty.");
            throw new IllegalArgumentException("Order must contain at least one item.");
        }
        System.out.println("[DEBUG] Order contains " + orderDto.items().size() + " item types.");

        // --- This is where the old faulty validation was. It has been removed. ---

        System.out.println("[DEBUG] Finding restaurant with ID: " + orderDto.vendorId());
        Restaurant restaurant = restaurantDao.findById(Long.valueOf(orderDto.vendorId()));
        if (restaurant == null || restaurant.getOperationalStatus() != OperationalStatus.OPEN) {
            System.out.println("[DEBUG] ERROR: Restaurant not found or is closed.");
            throw new IllegalArgumentException("Invalid Input: Restaurant is not available or does not exist.");
        }
        System.out.println("[DEBUG] Restaurant found: " + restaurant.getTitle());

        Order newOrder = new Order(customer, restaurant, orderDto.deliveryAddress());
        System.out.println("[DEBUG] New Order object created.");

        BigDecimal preliminarySubtotal = BigDecimal.ZERO;
        for (OrderDto.SubmitOrderItemDTO itemDto : orderDto.items()) {
            System.out.println("[DEBUG] Processing item ID: " + itemDto.itemId() + ", Quantity: " + itemDto.quantity());
            Item item = itemDao.findById(itemDto.itemId());

            if (item == null) throw new IllegalArgumentException("Invalid Input: Item with ID " + itemDto.itemId() + " not found.");
            if (item.getRestaurant().getId() != restaurant.getId()) throw new IllegalArgumentException("Invalid Input: Item '" + item.getTitle() + "' does not belong to the specified restaurant.");
            if (item.getCount() < itemDto.quantity()) throw new IllegalArgumentException("Not enough stock for item: " + item.getTitle());

            item.decreaseCount(itemDto.quantity());

            BigDecimal itemPrice = BigDecimal.valueOf(item.getPrice().getPrice());
            OrderItem orderItem = new OrderItem(newOrder, item.getId(), item.getTitle(), itemDto.quantity(), itemPrice);
            newOrder.addOrderItem(orderItem);
            preliminarySubtotal = preliminarySubtotal.add(orderItem.getTotalPriceForItem());
        }
        System.out.println("[DEBUG] All items processed successfully.");

        if (orderDto.couponId() != null) {
            // ... coupon logic ...
        }

        newOrder.setTaxFee(restaurant.getTaxFee() != null ? BigDecimal.valueOf(restaurant.getTaxFee()) : BigDecimal.ZERO);
        newOrder.setAdditionalFee(restaurant.getAdditionalFee() != null ? BigDecimal.valueOf(restaurant.getAdditionalFee()) : BigDecimal.ZERO);
        newOrder.setDeliveryFee(new BigDecimal("5.00"));
        System.out.println("[DEBUG] Fees have been set.");

        newOrder.calculateTotals();
        System.out.println("[DEBUG] Totals have been calculated.");

        orderDao.save(newOrder);
        System.out.println("[DEBUG] Order has been saved to the database. New Order ID: " + newOrder.getId());

        System.out.println("[DEBUG] --- Exiting submitOrder method successfully ---");
        return Optional.of(mapOrderToSchemaDTO(newOrder));
    }

    public Optional<OrderDto.OrderSchemaDTO> getOrderDetails(Long orderId, User authenticatedUser) {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            return Optional.empty();
        }

        boolean isTheCustomer = order.getCustomer().getId().equals(authenticatedUser.getId());
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;
        boolean isTheSeller = authenticatedUser instanceof Owner && ((Owner) authenticatedUser).getRestaurant() != null && ((Owner) authenticatedUser).getRestaurant().getId() == order.getRestaurant().getId();
        boolean isTheCourier = authenticatedUser instanceof Deliveryman && order.getDeliveryman() != null && order.getDeliveryman().getId().equals(authenticatedUser.getId());

        if (!isTheCustomer && !isAdmin && !isTheSeller && !isTheCourier) {
            throw new SecurityException("Forbidden: You are not authorized to view this order.");
        }

        return Optional.of(mapOrderToSchemaDTO(order));
    }

    public List<OrderDto.OrderSchemaDTO> getOrderHistory(User authenticatedUser, String searchFilter, String vendorFilter) {
        List<Order> orders = orderDao.findHistoryForUser(authenticatedUser.getId(), searchFilter, vendorFilter);
        return orders.stream().map(this::mapOrderToSchemaDTO).collect(Collectors.toList());
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
                order.getStatus().name(), order.getCreatedAt(), order.getUpdatedAt(),(order.getReview() != null) ? order.getReview().getId() : null
        );
    }
}