package Controller;

import com.sun.net.httpserver.HttpExchange;
import dao.OrderDao;
import dto.RestaurantDto;
import enums.OrderDeliveryStatus;
import enums.OrderRestaurantStatus;
import enums.OrderStatus;
import exception.ConflictException;
import exception.DeliveryAssigendException;
import exception.ForbiddenException;
import exception.NotFoundException;
import model.Deliveryman;
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
                    order.getCustomer().getId().intValue(), // Handle potential null customer
                    order.getRestaurant().getId(),
                    order.getCoupon_id(),
                    itemIds,
                    order.getRaw_price(),
                    order.getTax_fee(),
                    order.getAdditional_fee(),
                    order.getCourier_fee(),
                    order.getPay_price(),
                    order.getDeliveryman() != null ? order.getDeliveryman().getId().intValue():0,
                    order.getStatus().name().toLowerCase(),
                    order.getCreatedAt(),
                    order.getUpdatedAt()
            ));
        }
        return orderResponseDtos;
    }

    public RestaurantDto.OrderResponseDto changeOrderStatus(Deliveryman courier, int orderId,String deliveyStatus) throws Exception {
         Order order = orderDao.findById(orderId);
         if (order==null){
             throw new NotFoundException(404,"Resource not found");
         }
         if (!order.getRestaurantStatus().equals(OrderRestaurantStatus.SERVED)){
             throw new ForbiddenException(403);
         }
        OrderDeliveryStatus status=OrderDeliveryStatus.fromString(deliveyStatus);
         if (status.equals(OrderDeliveryStatus.ACCEPTED)) {

             if (!order.getDeliveryStatus().equals(OrderDeliveryStatus.BASE)) {
                 throw new DeliveryAssigendException(409);
             }
             order.setDeliveryman(courier);
         }
         else{
             if(order.getDeliveryman().getId()!=courier.getId()){
                 throw new ForbiddenException(403);
             }
         }
         order.setDeliveryStatus(status);
         order.updateStatus();
         orderDao.update(order);// should we update updated_at ?
        List<Integer> itemIds = new ArrayList<>();
        for (Item item : order.getItems()) {
            itemIds.add(item.getId());
        }
        return new RestaurantDto.OrderResponseDto(
                order.getId(),
                order.getDelivery_address(),
                order.getCustomer().getId().intValue(), // Handle potential null customer
                order.getRestaurant().getId(),
                order.getCoupon_id(),
                itemIds,
                order.getRaw_price(),
                order.getTax_fee(),
                order.getAdditional_fee(),
                order.getCourier_fee(),
                order.getPay_price(),
                (order.getDeliveryman() != null) ? order.getDeliveryman().getId().intValue():0,
                order.getStatus().name().toLowerCase(),
                order.getCreatedAt(),
                order.getUpdatedAt());

    }
}
