package util;

import enums.*;
import model.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestDataBuilder {

    public static class UserBuilder {
        private String name, phone, email;
        private String password = "password123";
        private Role role;
        private String bankName, accountNumber;

        public UserBuilder withRole(Role role) { this.role = role; return this; }
        public UserBuilder withName(String name) { this.name = name; return this; }
        public UserBuilder withPhone(String phone) { this.phone = phone; return this; }
        public UserBuilder withEmail(String email) { this.email = email; return this; }
        public UserBuilder withBankInfo(String bankName, String accountNumber) {
            this.bankName = bankName;
            this.accountNumber = accountNumber;
            return this;
        }

        public User build() {
            String address = "Default Address for " + name;
            return switch (role) {
                case BUYER -> new Customer(name, address, phone, email, password, null, null, null);
                case SELLER -> new Owner(name, address, phone, email, password, null, bankName, accountNumber);
                case COURIER -> new Deliveryman(name, address, phone, email, password, null, bankName, accountNumber);
                case ADMIN -> {
                    Customer adminAsCustomer = new Customer(name, address, phone, email, password, null, null, null);
                    adminAsCustomer.setRole(Role.ADMIN);
                    yield adminAsCustomer;
                }
            };
        }
    }

    public static class RestaurantBuilder {
        private Owner owner;
        private String title;
        private RestaurantCategory category;
        private ApprovalStatus approvalStatus = ApprovalStatus.WAITING;
        private OperationalStatus operationalStatus = OperationalStatus.CLOSED;
        private double averageRating = 0.0;
        private List<Menu> menus = new ArrayList<>();

        public RestaurantBuilder withOwner(Owner owner) { this.owner = owner; return this; }
        public RestaurantBuilder withTitle(String title) { this.title = title; return this; }
        public RestaurantBuilder withCategory(RestaurantCategory category) { this.category = category; return this; }
        public RestaurantBuilder withMenu(Menu menu) { this.menus.add(menu); return this; }
        public RestaurantBuilder withAverageRating(double rating) { this.averageRating = rating; return this; }
        public RestaurantBuilder isApproved() { this.approvalStatus = ApprovalStatus.REGISTERED; return this; }
        public RestaurantBuilder isOpen() { this.operationalStatus = OperationalStatus.OPEN; return this; }

        public Restaurant build() {
            Restaurant restaurant = new Restaurant(title, owner.getAddress(), owner.getPhoneNumber(), owner, 10, 5, null);
            restaurant.setCategory(category);
            restaurant.setApprovalStatus(approvalStatus);
            restaurant.setOperationalStatus(operationalStatus);
            restaurant.setAverageRating(averageRating);
            menus.forEach(restaurant::addMenu);
            return restaurant;
        }
    }

    public static class MenuBuilder {
        private String title;
        private List<Item> items = new ArrayList<>();

        public MenuBuilder withTitle(String title) { this.title = title; return this; }
        public MenuBuilder withItem(Item item) { this.items.add(item); return this; }

        public Menu build(Restaurant restaurant) {
            Menu menu = new Menu(restaurant, title);
            items.forEach(item -> {
                menu.addItem(item);
                item.addToMenu(menu);
            });
            return menu;
        }
    }

    public static class ItemBuilder {
        private String title;
        private String description = "A delicious menu item.";
        private int price;
        private int stockCount = 100;
        private ArrayList<String> keywords = new ArrayList<>(List.of("food"));

        public ItemBuilder withTitle(String title) { this.title = title; return this; }
        public ItemBuilder withPrice(int price) { this.price = price; return this; }
        public ItemBuilder withStock(int stock) { this.stockCount = stock; return this; }
        public ItemBuilder withKeywords(List<String> keywords) { this.keywords = new ArrayList<>(keywords); return this; }

        public Item build() {
            return new Item(title, description, price, stockCount, keywords, null);
        }
    }
}