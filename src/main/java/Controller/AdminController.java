package Controller;

import dto.*;
import enums.ApprovalStatus;
import enums.CouponType;
import enums.Role;
import exception.ForbiddenException;
import exception.InvalidInputException;
import exception.NotFoundException;
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
    TransactionDao transactionDao = new TransactionDao();
    CouponDao couponDao = new CouponDao();
    RestaurantDao restaurantDao = new RestaurantDao();

    private User findUserByPublicId(String publicId) throws Exception {
        User user = ownerDao.findByPublicId(publicId);
        if (user != null) return user;

        user = customerDao.findByPublicId(publicId);
        if (user != null) return user;

        user = deliverymanDao.findByPublicId(publicId);
        return user;
    }

    public List<AdminDto.UserSchemaDTO> getAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.addAll(customerDao.getAll());
        users.addAll(ownerDao.getAll());
        users.addAll(deliverymanDao.getAll());
        AdminDto.UserSchemaDTO userSchemaDTO;
        UserDto.RegisterRequestDTO.BankInfoDTO bankInfoForSchema = null;
        List<AdminDto.UserSchemaDTO> usersDTO = new ArrayList<>();
        for (User user : users) {
            if (user.getBankName() != null || user.getAccountNumber() != null) {
                bankInfoForSchema = new UserDto.RegisterRequestDTO.BankInfoDTO(user.getBankName(), user.getAccountNumber());
            }
            userSchemaDTO = new AdminDto.UserSchemaDTO(
                    user.getPublicId(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    user.isVerified() ? "approved" : "rejected",
                    bankInfoForSchema
            );
            usersDTO.add(userSchemaDTO);
        }
        return usersDTO;
    }

    public void updateUserApprovalStatus(String userToUpdatePublicId, String newStatus) throws Exception {
        // 1. Find the user by checking all specific DAOs.
        User user = findUserByPublicId(userToUpdatePublicId);
        if (user == null) {
            throw new NotFoundException(404, "user not found");
        }

        boolean isApproved = "approved".equalsIgnoreCase(newStatus);

        // 2. Check for conflicts (e.g., approving an already approved user).
        if (user.isVerified() == isApproved) {
            throw new ForbiddenException(403);
        }

        // 3. Set the new verification status on the user object itself.
        user.setVerified(isApproved);

        // 4. Use the correct specific DAO to save the updated user.
        if (user instanceof Owner owner) {
            ownerDao.update(owner);
        } else if (user instanceof Deliveryman courier) {
            deliverymanDao.update(courier);
        } else {
            // If the user is a type that doesn't need verification, throw an error.
            throw new InvalidInputException(400, "User role cannot be approved/rejected.");
        }
    }
    public void updateRestaurantStatus(String restauratnID, String newStatus) throws Exception {
        int id;
        try {
            id = Integer.parseInt(restauratnID);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(400, "id");
        }
        Restaurant restaurant =restaurantDao.findById((long) id);
        if (restaurant == null) {
            throw new InvalidInputException(404, "user not found");
        }
        switch (restaurant.getApprovalStatus()) {
            case ApprovalStatus.WAITING, ApprovalStatus.SUSPENDED:
                switch (newStatus.toLowerCase()) {
                    case "registered":
                        restaurant.setApprovalStatus(ApprovalStatus.REGISTERED);
                        break;
                    case "rejected":
                        restaurant.setApprovalStatus(ApprovalStatus.REJECTED);
                        break;
                    default:
                        throw new ForbiddenException(403);
                }
                break;
            case ApprovalStatus.REGISTERED:
                switch (newStatus.toLowerCase()) {
                    case "suspended":
                        restaurant.setApprovalStatus(ApprovalStatus.SUSPENDED);
                        break;
                    case "rejected":
                        restaurant.setApprovalStatus(ApprovalStatus.REJECTED);
                        break;
                    default:
                        throw new ForbiddenException(403);
                }
                break;
                case ApprovalStatus.REJECTED:
                switch (newStatus.toLowerCase()) {
                    case "registered":
                        restaurant.setApprovalStatus(ApprovalStatus.REGISTERED);
                        break;
                    default:
                        throw new ForbiddenException(403);
                }
                break;
        }restaurantDao.update(restaurant);
    }

    public List<AdminDto.OrderSchemaDTO> getAllOrders(String searchFilter) throws Exception {
        List<Order> orders = orderDao.findHistoryForAdmin(searchFilter);
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        List<AdminDto.OrderSchemaDTO> ordersDTO = new ArrayList<>();
        for (Order order : orders) {
            ordersDTO.add(mapOrderToSchemaDTO(order));
        }
        return ordersDTO;
    }

    public List<AdminDto.TransactionSchemaDTO> getAllTransactions(String searchFilter) throws Exception {
        List<Transaction> transactions = transactionDao.findHistoryForAdmin(searchFilter);
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }
        List<AdminDto.TransactionSchemaDTO> transactionsDTO = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionsDTO.add(mapTransactionToDto(transaction));
        }
        return transactionsDTO;
    }

    public List<AdminDto.CouponSchemaDTO> getAllCoupons() {
        List<Coupon> coupons = couponDao.getAll();
        if (coupons == null || coupons.isEmpty()) {
            return new ArrayList<>();
        }
        List<AdminDto.CouponSchemaDTO> couponsDTO = new ArrayList<>();
        for (Coupon coupon : coupons) {
            couponsDTO.add(mapCouponToSchemaDto(coupon));
        }
        return couponsDTO;
    }

    public AdminDto.CouponSchemaDTO createCoupon(CouponDto.CouponInputSchemaDTO couponDto) throws Exception {
        checkCouponInfo(couponDto);
        Coupon coupon = new Coupon(couponDto.couponCode(), CouponType.valueOf(couponDto.type().toUpperCase()), couponDto.value(), couponDto.minPrice(), couponDto.userCount(), couponDto.startDate(), couponDto.endDate());
        couponDao.save(coupon);
        return mapCouponToSchemaDto(coupon);
    }

    public AdminDto.CouponSchemaDTO updateCoupon(CouponDto.CouponInputSchemaDTO couponDto, int id) throws Exception {
        Coupon coupon = couponDao.findById(id);
        if (coupon == null) {
            throw new NotFoundException(404, "coupon not found");
        }
        checkCouponInfo(couponDto);
        coupon.setCouponCode(couponDto.couponCode());
        coupon.setType(CouponType.valueOf(couponDto.type().toUpperCase()));
        coupon.setValue(couponDto.value());
        coupon.setMinPrice(couponDto.minPrice());
        coupon.setUserCount(couponDto.userCount());
        coupon.setStartDate(couponDto.startDate());
        coupon.setEndDate(couponDto.endDate());
        couponDao.update(coupon);
        return mapCouponToSchemaDto(coupon);
    }

    public void deleteCoupon(int id) throws Exception {
        Coupon coupon = couponDao.findById(id);
        if (coupon == null) {
            throw new NotFoundException(404, "coupon not found");
        }
        couponDao.delete(coupon);
        return;
    }

    public AdminDto.CouponSchemaDTO getCouponDetails(int id) throws Exception {
        Coupon coupon = couponDao.findById(id);
        if (coupon == null) {
            throw new NotFoundException(404, "coupon not found");
        }
        return mapCouponToSchemaDto(coupon);
    }
    public List<RestaurantDto.RegisterReponseRestaurantDto> getAllRestaurants(String searchFilter) throws Exception {
        List<Restaurant> restaurants = restaurantDao.findRestaurantForAdmin(searchFilter);
        if (restaurants == null || restaurants.isEmpty()) {
            return new ArrayList<RestaurantDto.RegisterReponseRestaurantDto>();
        }
        List<RestaurantDto.RegisterReponseRestaurantDto> restaurantsDTO = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            restaurantsDTO.add(new RestaurantDto.RegisterReponseRestaurantDto(restaurant.getId(),restaurant.getTitle(),restaurant.getAddress(),restaurant.getPhoneNumber(),restaurant.getLogoBase64(),restaurant.getTaxFee(),restaurant.getAdditionalFee(),restaurant.getApprovalStatus().name()));
        }
        return restaurantsDTO;
    }

    public Admin CheckAdminValidation(String token) throws Exception {
        String[] info = token.trim().split("_");
        int id;
        try {
            id = Integer.parseInt(info[0]);
            Admin admin = adminDao.findById((long) id);
            if (admin != null) {
                if (!admin.getPassword().equals(info[1])) {
                    throw new AuthController.AuthenticationException("Unauthorized request");
                }
            } else {
                throw new AuthController.AuthenticationException("Unauthorized request");
            }
            return admin;
        } catch (NumberFormatException e) {
            throw new AuthController.AuthenticationException("Unauthorized request");
        }
    }
    public Admin adminLogin(String phone,String password) throws Exception {
        int id;
        try {
            id = Integer.parseInt(phone);
            Admin admin = adminDao.findById((long) id);
            if (admin != null) {
                if (!admin.getPassword().equals(password)) {
                    throw new AuthController.AuthenticationException("Unauthorized request");
                }
            } else {
                return null;
            }
            return admin;
        } catch (NumberFormatException e) {
        return null;
        }
    }

    private AdminDto.OrderSchemaDTO mapOrderToSchemaDTO(Order order) {
        if (order == null) return null;
        List<AdminDto.OrderItemDto> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            items.add(new AdminDto.OrderItemDto(item.getItemId(),item.getItemName(),item.getPricePerItem(),item.getTotalPriceForItem(),item.getQuantity()));
        }
        return new AdminDto.OrderSchemaDTO(
                order.getId(), order.getDeliveryAddress(), order.getCustomer().getId(),order.getCustomer().getFullName(),
                order.getRestaurant().getId(), order.getRestaurant().getTitle(),(order.getCoupon() != null) ? order.getCoupon().getId() : null,
                items, order.getSubtotalPrice(), order.getTaxFee(), order.getDeliveryFee(),
                order.getAdditionalFee(), order.getTotalPrice(),
                (order.getDeliveryman() != null) ? order.getDeliveryman().getId() : null,
                order.getStatus().name(),order.getRestaurantStatus().name(),order.getDeliveryStatus().name(), order.getCreatedAt().toString(), order.getUpdatedAt().toString()
        );
    }

    private AdminDto.TransactionSchemaDTO mapTransactionToDto(Transaction tx) {
        if (tx == null) return null;
        return new AdminDto.TransactionSchemaDTO(
                tx.getId(),
                (tx.getOrder() != null) ? tx.getOrder().getId() : null,
                tx.getUser().getId(),
                tx.getAmount(),
                (tx.getMethod() != null) ? tx.getMethod().name() : null,
                tx.getStatus().name(),
                tx.getType().name(),
                tx.getCreatedAt().toString()
        );
    }

    private AdminDto.CouponSchemaDTO mapCouponToSchemaDto(Coupon cp) {
        if (cp == null) return null;
        return new AdminDto.CouponSchemaDTO(cp.getId(),
                cp.getCouponCode(),
                cp.getType().name(),
                cp.getValue(),
                cp.getMinPrice(),
                cp.getUserCount(),
                cp.getStartDate().toString(),
                cp.getEndDate().toString());
    }

    private void checkCouponInfo(CouponDto.CouponInputSchemaDTO couponDto) throws Exception {
        if (couponDto.couponCode() == null || couponDto.couponCode().isEmpty()) {
            throw new InvalidInputException(400, "couponCode");
        }
        if (!(couponDto.type().toUpperCase().equals("FIXED") || couponDto.type().toUpperCase().equals("PERCENT"))) {
            throw new InvalidInputException(400, "couponType");
        }
        if (couponDto.value().doubleValue() <= 0) {
            throw new InvalidInputException(400, "value");
        }
        if (couponDto.minPrice() <= 0) {
            throw new InvalidInputException(400, "minPrice");
        }
        if (couponDto.userCount() <= 0) {
            throw new InvalidInputException(400, "userCount");
        }
        if (couponDto.startDate().isAfter(couponDto.endDate())) {
            throw new InvalidInputException(400, "Date");
        }
    }

}


