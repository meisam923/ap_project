package util;

import dao.CustomerDao;
import dao.DeliverymanDao;
import dao.OwnerDao;
import enums.RestaurantCategory;
import enums.Role;
import model.*;

import java.util.List;

public class DataSeeder {

    private static final OwnerDao ownerDao = new OwnerDao();
    private static final CustomerDao customerDao = new CustomerDao();
    private static final DeliverymanDao deliverymanDao = new DeliverymanDao();

    public static void main(String[] args) {
        try {
            Class.forName("util.JpaUtil");
            System.out.println("JPA Initialized for Seeding.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); return;
        }

        System.out.println("Seeding database with comprehensive data set...");

        try {
            // --- USERS ---
            Owner seller1 = (Owner) new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Chef Antoine").withPhone("09888888888").withEmail("antoine@bistro.com").withBankInfo("Bistro Bank", "ACC1").build();
            Owner seller2 = (Owner) new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Jane Austen").withPhone("09888888882").withEmail("jane@cafe.com").withBankInfo("Cafe Bank", "ACC2").build();
            Customer buyer1 = (Customer) new TestDataBuilder.UserBuilder().withRole(Role.BUYER).withName("Test Buyer Emily").withPhone("09112223344").withEmail("emily@example.com").build();

            // --- RESTAURANTS ---
            Restaurant bistro = new TestDataBuilder.RestaurantBuilder().withOwner(seller1).withTitle("Antoine's Bistro").withCategory(RestaurantCategory.TRADITIONAL).withAverageRating(4.7).isApproved().isOpen().build();
            Restaurant cafe = new TestDataBuilder.RestaurantBuilder().withOwner(seller2).withTitle("The Reading Nook Cafe").withCategory(RestaurantCategory.CAFE).withAverageRating(4.9).isApproved().isOpen().build();

            // --- ITEMS (Now correctly linked to their restaurants) ---
            Item steak = new TestDataBuilder.ItemBuilder().withTitle("Steak Frites").withPrice(25).forRestaurant(bistro).build();
            Item soup = new TestDataBuilder.ItemBuilder().withTitle("French Onion Soup").withPrice(9).forRestaurant(bistro).build();
            Item latte = new TestDataBuilder.ItemBuilder().withTitle("Latte").withPrice(5).forRestaurant(cafe).build();
            Item scone = new TestDataBuilder.ItemBuilder().withTitle("Scone").withPrice(4).forRestaurant(cafe).build();

            // --- MENUS ---
            Menu bistroMenu = new TestDataBuilder.MenuBuilder().withTitle("Main Menu").withItem(steak).withItem(soup).build(bistro);
            Menu cafeMenu = new TestDataBuilder.MenuBuilder().withTitle("Beverages & Snacks").withItem(latte).withItem(scone).build(cafe);

            bistro.addMenu(bistroMenu);
            cafe.addMenu(cafeMenu);

            seller1.setRestaurant(bistro);
            seller2.setRestaurant(cafe);

            // --- SAVE TO DB ---
            ownerDao.save(seller1);
            ownerDao.save(seller2);
            customerDao.save(buyer1);

            System.out.println("\nDatabase seeding complete!");

        } catch (Exception e) {
            System.err.println("An error occurred during database seeding:");
            e.printStackTrace();
        } finally {
            JpaUtil.closeEntityManagerFactory();
        }
    }
}
