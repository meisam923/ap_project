package model;

import enums.RestaurantCategory;
import enums.ApprovalStatus;   // Changed from RestaurantStatus
import enums.OperationalStatus; // New enum
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Embedded
    private Location location;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", unique = true)
    private Owner owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RestaurantCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.WAITING; // Default for new restaurants

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false)
    private OperationalStatus operationalStatus = OperationalStatus.CLOSED; // Default to closed

    private Integer taxFee;
    private Integer additionalFee;
    private String logoBase64;
    private Double averageRating = 0.0;

    public Restaurant() {}

    public Restaurant(String title, String address, String phoneNumber, Owner owner, Integer taxFee, Integer additionalFee, String logoBase64) {
        this.title = title;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.owner = owner;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
        this.logoBase64 = logoBase64;
        this.approvalStatus = ApprovalStatus.WAITING;
        this.operationalStatus = OperationalStatus.CLOSED;
    }

    public Menu getMenu(String title) {
        if (this.menus == null || title == null) {
            return null;
        }
        for (Menu menu : this.menus) {
            if (title.equals(menu.getTitle())) {
                return menu;
            }
        }
        return null;
    }

    public void addMenu(Menu menu) {
        this.menus.add(menu);
        menu.setRestaurant(this);
    }

    public void removeMenu(String menuTitle) {
        if (this.menus == null || menuTitle == null) {
            return;
        }
        this.menus.removeIf(menu -> menuTitle.equals(menu.getTitle()));
    }

}