package Controller;

import dao.OrderDao;
import dto.NotificationDto;
import model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationController {
    private final OrderDao orderDao = new OrderDao();


    public List<NotificationDto.NotificationSchemaDTO> getNotificationsForUser(User user) throws Exception {
        List<NotificationDto.NotificationSchemaDTO> notifications = new ArrayList<>();

        switch (user.getRole()) {
            case BUYER:
                notifications.addAll(generateBuyerNotifications((Customer) user));
                break;
            case SELLER:
                notifications.addAll(generateSellerNotifications((Owner) user));
                break;
            case COURIER:
                notifications.addAll(generateCourierNotifications((Deliveryman) user));
                break;
        }

        return notifications.stream()
                .sorted(Comparator.comparing(NotificationDto.NotificationSchemaDTO::timestamp).reversed())
                .collect(Collectors.toList());
    }


    private List<NotificationDto.NotificationSchemaDTO> generateBuyerNotifications(Customer customer) {
        List<Order> userOrders = orderDao.findHistoryForUser(customer.getId(), null, null);
        List<NotificationDto.NotificationSchemaDTO> notifications = new ArrayList<>();

        for (Order order : userOrders) {
            notifications.add(new NotificationDto.NotificationSchemaDTO(
                    "order-" + order.getId() + "-paid",
                    "Payment for order #" + order.getId() + " was successful.",
                    order.getCreatedAt().plusSeconds(1),
                    false
            ));

            if (order.getStatus() == enums.OrderStatus.FINDING_COURIER) {
                notifications.add(new NotificationDto.NotificationSchemaDTO(
                        "order-" + order.getId() + "-accepted",
                        "The restaurant has accepted your order #" + order.getId() + ".",
                        order.getUpdatedAt(),
                        false
                ));
            }

            if (order.getStatus() == enums.OrderStatus.ON_THE_WAY && order.getDeliveryman() != null) {
                notifications.add(new NotificationDto.NotificationSchemaDTO(
                        "order-" + order.getId() + "-on-the-way",
                        "A courier is on the way with your order #" + order.getId() + "!",
                        order.getUpdatedAt(),
                        false
                ));
            }

            if (order.getStatus() == enums.OrderStatus.COMPLETED) {
                notifications.add(new NotificationDto.NotificationSchemaDTO(
                        "order-" + order.getId() + "-delivered",
                        "Your order #" + order.getId() + " has been delivered. Enjoy!",
                        order.getUpdatedAt(),
                        false
                ));
            }
        }
        return notifications;
    }

    private List<NotificationDto.NotificationSchemaDTO> generateSellerNotifications(Owner owner) throws Exception {
        if (owner.getRestaurant() == null) {
            return new ArrayList<>();
        }
        List<Order> restaurantOrders = orderDao.findHistoryForRestaurant(owner.getRestaurant().getId(), new java.util.HashMap<>());
        List<NotificationDto.NotificationSchemaDTO> notifications = new ArrayList<>();

        for (Order order : restaurantOrders) {
            if (order.getStatus() == enums.OrderStatus.WAITING_VENDOR) {
                notifications.add(new NotificationDto.NotificationSchemaDTO(
                        "new-order-" + order.getId(),
                        "You have a new order (#" + order.getId() + ") from " + order.getCustomer().getFullName() + ".",
                        order.getUpdatedAt(),
                        false
                ));
            }
        }
        return notifications;
    }


    private List<NotificationDto.NotificationSchemaDTO> generateCourierNotifications(Deliveryman courier) {
        if (!courier.isVerified()) {
            return new ArrayList<>();
        }
        List<Order> availableOrders = orderDao.findAvailableForDelivery();
        List<NotificationDto.NotificationSchemaDTO> notifications = new ArrayList<>();

        for (Order order : availableOrders) {
            notifications.add(new NotificationDto.NotificationSchemaDTO(
                    "delivery-" + order.getId(),
                    "New delivery available! Pick up from " + order.getRestaurant().getTitle() + ".",
                    order.getUpdatedAt(),
                    false
            ));
        }
        return notifications;
    }
}