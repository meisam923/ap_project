package Controller;

import dao.CustomerDao;
import dao.OrderDao;
import dao.TransactionDao;
import dto.PaymentDto;
import enums.OrderStatus;
import enums.TransactionMethod;
import enums.TransactionStatus;
import enums.TransactionType;
import model.Customer;
import model.Order;
import model.Transaction;
import model.User;

import java.util.List;
import java.util.stream.Collectors;

public class PaymentController {
    private final CustomerDao customerDao = new CustomerDao();
    private final OrderDao orderDao = new OrderDao();
    private final TransactionDao transactionDao = new TransactionDao();

    public void topUpWallet(User user, PaymentDto.TopUpRequestDTO topUpDto) {
        if (!(user instanceof Customer)) {
            throw new SecurityException("Forbidden: Only customers can top up a wallet.");
        }
        if (topUpDto.amount() == null || topUpDto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid Input: Top-up amount must be positive.");
        }

        Customer customer = (Customer) user;
        customer.addToWallet(topUpDto.amount());
        customerDao.update(customer);

        Transaction tx = new Transaction(customer, null, topUpDto.amount(), TransactionType.TOP_UP, TransactionMethod.ONLINE, TransactionStatus.SUCCESS);
        transactionDao.save(tx);
    }

    public PaymentDto.TransactionSchemaDTO processPayment(User user, PaymentDto.PaymentRequestDTO paymentDto) {
        if (!(user instanceof Customer)) {
            throw new SecurityException("Forbidden: Only customers can make payments.");
        }
        Customer customer = (Customer) user;

        Order order = orderDao.findById(paymentDto.orderId());
        if (order == null) throw new NotFoundException("Order not found.");
        if (!order.getCustomer().getId().equals(customer.getId())) throw new SecurityException("Forbidden: Cannot pay for another user's order.");
        if (order.getStatus() != OrderStatus.SUBMITTED) throw new ConflictException("Order is not in a payable state.");

        TransactionMethod method = TransactionMethod.valueOf(paymentDto.method().toUpperCase());
        Transaction transaction;

        if (method == TransactionMethod.WALLET) {
            if (customer.getWalletBalance().compareTo(order.getTotalPrice()) < 0) {
                transaction = new Transaction(customer, order, order.getTotalPrice(), TransactionType.PAYMENT, method, TransactionStatus.FAILED);
                transactionDao.save(transaction);
                throw new ConflictException("Insufficient wallet balance.");
            }
            customer.subtractFromWallet(order.getTotalPrice());
            customerDao.update(customer);
        } else if (method == TransactionMethod.ONLINE) {
            System.out.println("Simulating successful online payment for order " + order.getId());
        }

        order.setStatus(OrderStatus.WAITING_VENDOR);
        orderDao.update(order);

        transaction = new Transaction(customer, order, order.getTotalPrice(), TransactionType.PAYMENT, method, TransactionStatus.SUCCESS);
        transactionDao.save(transaction);

        return mapTransactionToDto(transaction);
    }

    public List<PaymentDto.TransactionSchemaDTO> getTransactionHistory(User user) {
        List<Transaction> transactions = transactionDao.findByUser(user.getId());
        return transactions.stream()
                .map(this::mapTransactionToDto)
                .collect(Collectors.toList());
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

    public static class NotFoundException extends RuntimeException { public NotFoundException(String message) { super(message); } }
    public static class ConflictException extends RuntimeException { public ConflictException(String message) { super(message); } }
}