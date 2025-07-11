package model;

import enums.OrderDeliveryStatus;
import enums.OrderRestaurantStatus;
import enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "deliveryman_id")
    private Deliveryman deliveryman;

    @Column(name = "delivery_address")
    private String delivery_address;

    @Column(name = "coupon_id")
    private Integer coupon_id;


    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<Item> Items = new ArrayList<>();

    @Column(name = "raw_price", nullable = false)
    private int raw_price;

    @Column(name = "tax_fee", nullable = false)
    private int tax_fee;

    @Column(name = "additional_fee", nullable = false)
    private int additional_fee;

    @Column(name = "courier_fee", nullable = false)
    private int courier_fee;

    @Column(name = "pay_price", nullable = false)
    private int pay_price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "restaurant_status", nullable = false)
    private OrderRestaurantStatus restaurantStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private OrderDeliveryStatus deliveryStatus;

    @Column(name = "created_at", updatable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public Order() {}

    public Order(Customer customer,
                 Restaurant restaurant) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.restaurantStatus=OrderRestaurantStatus.BASE;
        this.deliveryStatus=OrderDeliveryStatus.BASE;
    }

    public void updateStatus(){
        switch (this.restaurantStatus) {
            case BASE:
                this.status = OrderStatus.SUBMITTED;
                this.deliveryStatus=OrderDeliveryStatus.BASE;
                break;

            case ACCEPTED:
                this.status=OrderStatus.WAITING_VENDOR;
                this.deliveryStatus=OrderDeliveryStatus.BASE;
                break;

            case REJECTED:
                this.status=OrderStatus.CANCELLED;
                this.deliveryStatus=OrderDeliveryStatus.BASE;
                break;

            case SERVED:
                switch (this.deliveryStatus) {
                    case BASE:
                        this.status=OrderStatus.FINDING_COURIER;
                        break;

                    case DELIVERED:
                        this.status=OrderStatus.COMPLETED;
                        break;

                    default:
                        this.status=OrderStatus.ON_THE_WAY; // it may have some nuances//
                }
        }
    }
}
