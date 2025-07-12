package Controller;

import dto.OrderDto;
import dto.UserDto;
import enums.Role;
import exception.ForbiddenException;
import exception.InvalidInputException;
import jakarta.persistence.Id;
import model.*;
import dao.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminController {
    CustomerDao customerDao = new CustomerDao();
    DeliverymanDao deliverymanDao = new DeliverymanDao();
    OwnerDao ownerDao = new OwnerDao();
    AdminDao adminDao = new AdminDao();
    IDao<User, Long> userDao = new UserDao();
    OrderDao orderDao = new OrderDao();

    public List<UserDto.UserSchemaDTO> getAllUsers()throws Exception{
        List<User> users = new ArrayList<>();
        users.addAll(customerDao.getAll());
        users.addAll(ownerDao.getAll());
        users.addAll(deliverymanDao.getAll());
        UserDto.UserSchemaDTO userSchemaDTO;
        UserDto.RegisterRequestDTO.BankInfoDTO  bankInfoForSchema = null;
        List<UserDto.UserSchemaDTO> usersDTO = new ArrayList<>();
        for(User user : users){
            if (user.getBankName() != null || user.getAccountNumber() != null) {
                bankInfoForSchema = new UserDto.RegisterRequestDTO.BankInfoDTO(user.getBankName(), user.getAccountNumber());
            }
            userSchemaDTO = new UserDto.UserSchemaDTO(
                    user.getPublicId(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    bankInfoForSchema
            );
            usersDTO.add(userSchemaDTO);
        }
        return usersDTO;
    }

    public void updateUserApprovalStatus(String userToUpdatePublicId, String newStatus) throws Exception {
        int id;
        try{
            id = Integer.parseInt(userToUpdatePublicId);
        } catch (NumberFormatException e){
            throw new InvalidInputException(400,"id");
        }
        User user = userDao.findById((long)id);
        if (user == null) {
            throw new InvalidInputException(404,"user not found");
        }
        switch (newStatus){
            case "approved":
                if (user.isVerified()) {
                    throw new ForbiddenException(403);
                }
                else  {
                    user.setVerified(true);
                }
                break;
            case "rejected":
                if (user.isVerified()) {
                    user.setVerified(false);
                }
                else {
                    throw new ForbiddenException(403);
                }

        }
        userDao.update(user);
    }

    public List <OrderDto.OrderSchemaDTO> getAllOrders(String searchFilter, String vendorFilter, String courierFilter, String customerFilter , String statusFilter) throws Exception{
        List<Order> orders = orderDao.findHistoryForAdmin(searchFilter,vendorFilter,courierFilter,customerFilter,statusFilter);
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();}
        List<OrderDto.OrderSchemaDTO> ordersDTO = new ArrayList<>();
        for (Order order : orders) {
            ordersDTO.add(mapOrderToSchemaDTO(order));
        }
        return ordersDTO;
    }


    public Admin CheckAdminValidation (String token) throws Exception {
        String[] info = token.trim().split("_");
        int id;
            try {
                id= Integer.parseInt(info[0]);
                Admin admin = adminDao.findById((long)id);
                if (admin != null) {
                    if (!admin.getPassword().equals(info[1])) {
                        throw new AuthController.AuthenticationException("Unauthorized request");
                    }
                }else{
                    throw new AuthController.AuthenticationException("Unauthorized request");
                }
                return admin;
            } catch (NumberFormatException e) {
                throw new AuthController.AuthenticationException("Unauthorized request");
            }
        }
    private OrderDto.OrderSchemaDTO mapOrderToSchemaDTO(Order order) {
        if (order == null) return null;
        List<Integer> itemIds = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            itemIds.add(item.getItemId());
        }
        return new OrderDto.OrderSchemaDTO(
                order.getId(), order.getDeliveryAddress(), order.getCustomer().getId(),
                order.getRestaurant().getId(), (order.getCoupon() != null) ? order.getCoupon().getId() : null,
                itemIds, order.getSubtotalPrice(), order.getTaxFee(), order.getDeliveryFee(),
                order.getAdditionalFee(), order.getTotalPrice(),
                (order.getDeliveryman() != null) ? order.getDeliveryman().getId() : null,
                order.getStatus().name(), order.getCreatedAt(), order.getUpdatedAt()
        );
    }
    }


