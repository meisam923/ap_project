package Controller;

import com.sun.net.httpserver.HttpExchange;
import dao.OrderDao;
import dto.RestaurantDto;
import enums.OrderStatus;
import model.Item;
import model.Order;

import java.util.ArrayList;
import java.util.List;

public class CourierController {
    private final OrderDao orderDao=new OrderDao();

    public List<RestaurantDto.OrderResponseDto> getAvailableDeliveryRequest() throws Exception {
        List<Order> orders = orderDao.findByStatus(OrderStatus.FINDING_COURIER);
        List<RestaurantDto.OrderResponseDto> orderResponseDtos = new ArrayList<>();
        for (Order order : orders) {
            List<Integer> itemIds = new ArrayList<>();
            for (Item item : order.getItems()) {
                itemIds.add(item.getId());
            }

            orderResponseDtos.add(new RestaurantDto.OrderResponseDto(
                    order.getId(),
                    order.getDelivery_address(),
                    order.getCustomer().getId(), // Handle potential null customer
                    order.getRestaurant().getId(),
                    order.getCoupon_id(),
                    itemIds,
                    order.getRaw_price(),
                    order.getTax_fee(),
                    order.getAdditional_fee(),
                    order.getCourier_fee(),
                    order.getPay_price(),
                    order.getDeliveryman().getId(),
                    order.getStatus().name(),
                    order.getCreatedAt(),
                    order.getUpdatedAt()
            ));
        }
        return orderResponseDtos;
    }
}
