package Controller;

import dto.CouponDto;
import dto.OrderDto;
import dto.PaymentDto;
import dto.UserDto;
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

    public List<UserDto.UserSchemaDTO> getAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        users.addAll(customerDao.getAll());
        users.addAll(ownerDao.getAll());
        users.addAll(deliverymanDao.getAll());
        UserDto.UserSchemaDTO userSchemaDTO;
        UserDto.RegisterRequestDTO.BankInfoDTO bankInfoForSchema = null;
        List<UserDto.UserSchemaDTO> usersDTO = new ArrayList<>();
        for (User user : users) {
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
        try {
            id = Integer.parseInt(userToUpdatePublicId);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(400, "id");
        }
        User user = userDao.findById((long) id);
        if (user == null) {
            throw new InvalidInputException(404, "user not found");
        }
        switch (newStatus) {
            case "approved":
                if (user.isVerified()) {
                    throw new ForbiddenException(403);
                } else {
                    user.setVerified(true);
                }
                break;
            case "rejected":
                if (user.isVerified()) {
                    user.setVerified(false);
                } else {
                    throw new ForbiddenException(403);
                }

        }
        userDao.update(user);
    }

    public List<OrderDto.OrderSchemaDTO> getAllOrders(String searchFilter, String vendorFilter, String courierFilter, String customerFilter, String statusFilter) throws Exception {
        List<Order> orders = orderDao.findHistoryForAdmin(searchFilter, vendorFilter, courierFilter, customerFilter, statusFilter);
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        List<OrderDto.OrderSchemaDTO> ordersDTO = new ArrayList<>();
        for (Order order : orders) {
            ordersDTO.add(mapOrderToSchemaDTO(order));
        }
        return ordersDTO;
    }

    public List<PaymentDto.TransactionSchemaDTO> getAllTransactions(String searchFilter, String userFilter, String methodFilter, String statusFilter) throws Exception {
        List<Transaction> transactions = transactionDao.findHistoryForAdmin(searchFilter, userFilter, methodFilter, statusFilter);
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }
        List<PaymentDto.TransactionSchemaDTO> transactionsDTO = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionsDTO.add(mapTransactionToDto(transaction));
        }
        return transactionsDTO;
    }

    public List<CouponDto.CouponSchemaDTO> getAllCoupons() {
        List<Coupon> coupons = couponDao.getAll();
        if (coupons == null || coupons.isEmpty()) {
            return new ArrayList<>();
        }
        List<CouponDto.CouponSchemaDTO> couponsDTO = new ArrayList<>();
        for (Coupon coupon : coupons) {
            couponsDTO.add(mapCouponToSchemaDto(coupon));
        }
        return couponsDTO;
    }

    public CouponDto.CouponSchemaDTO createCoupon(CouponDto.CouponInputSchemaDTO couponDto) throws Exception {
        checkCouponInfo(couponDto);
        Coupon coupon = new Coupon(couponDto.couponCode(), CouponType.valueOf(couponDto.type().toUpperCase()), couponDto.value(), couponDto.minPrice(), couponDto.userCount(), couponDto.startDate(), couponDto.endDate());
        couponDao.save(coupon);
        return mapCouponToSchemaDto(coupon);
    }

    public CouponDto.CouponSchemaDTO updateCoupon(CouponDto.CouponInputSchemaDTO couponDto, int id) throws Exception {
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

    public CouponDto.CouponSchemaDTO getCouponDetails(int id) throws Exception {
        Coupon coupon = couponDao.findById(id);
        if (coupon == null) {
            throw new NotFoundException(404, "coupon not found");
        }
        return mapCouponToSchemaDto(coupon);
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

    private PaymentDto.TransactionSchemaDTO mapTransactionToDto(Transaction tx) {
        if (tx == null) return null;
        return new PaymentDto.TransactionSchemaDTO(
                tx.getId(),
                (tx.getOrder() != null) ? tx.getOrder().getId() : null,
                tx.getUser().getId(),
                tx.getAmount(),
                (tx.getMethod() != null) ? tx.getMethod().name() : null,
                tx.getStatus().name(),
                tx.getType().name(),
                tx.getCreatedAt()
        );
    }

    private CouponDto.CouponSchemaDTO mapCouponToSchemaDto(Coupon cp) {
        if (cp == null) return null;
        return new CouponDto.CouponSchemaDTO(cp.getId(),
                cp.getCouponCode(),
                cp.getType().name(),
                cp.getValue(),
                cp.getMinPrice(),
                cp.getUserCount(),
                cp.getStartDate(),
                cp.getEndDate());
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


