package util;

import dao.UserDao;
import enums.RestaurantCategory;
import enums.Role;
import model.*;

import java.util.List;

public class DataSeeder {

    private static final UserDao userDao = new UserDao();

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
            User seller1 = new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Chef Antoine").withPhone("09888888888").withEmail("antoine@bistro.com").withBankInfo("Bistro Bank", "ACC1").build();
            User seller2 = new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Jane Austen").withPhone("09888888882").withEmail("jane@cafe.com").withBankInfo("Cafe Bank", "ACC2").build();
            User seller3 = new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Captain Ahab").withPhone("09888888884").withEmail("ahab@squid.com").withBankInfo("Whaler's Credit", "ACC3").build();
            User buyer1 = new TestDataBuilder.UserBuilder().withRole(Role.BUYER).withName("Test Buyer Emily").withPhone("09112223344").withEmail("emily@example.com").build();
            User courier1 = new TestDataBuilder.UserBuilder().withRole(Role.COURIER).withName("Speedy Dave").withPhone("09333334444").withEmail("dave@delivery.com").withBankInfo("Courier Credit", "ACC4").build();
            User admin1 = new TestDataBuilder.UserBuilder().withRole(Role.ADMIN).withName("Site Admin").withPhone("09000000000").withEmail("admin@app.com").build();

            // --- ITEMS ---
            Item steak = new TestDataBuilder.ItemBuilder().withTitle("Steak Frites").withPrice(25).withStock(50).withKeywords(List.of("beef", "classic")).build();
            Item soup = new TestDataBuilder.ItemBuilder().withTitle("French Onion Soup").withPrice(9).withStock(100).withKeywords(List.of("soup", "starter")).build();
            Item latte = new TestDataBuilder.ItemBuilder().withTitle("Latte").withPrice(5).withStock(200).withKeywords(List.of("coffee", "drink")).build();
            Item scone = new TestDataBuilder.ItemBuilder().withTitle("Scone").withPrice(4).withStock(80).withKeywords(List.of("pastry", "snack")).build();
            Item fishAndChips = new TestDataBuilder.ItemBuilder().withTitle("Fish and Chips").withPrice(18).withStock(40).withKeywords(List.of("fried", "seafood")).build();
            Item clamChowder = new TestDataBuilder.ItemBuilder().withTitle("Clam Chowder").withPrice(9).withStock(60).withKeywords(List.of("soup", "seafood")).build();

            // --- RESTAURANTS & MENUS ---
            Restaurant bistro = new TestDataBuilder.RestaurantBuilder()
                    .withOwner((Owner) seller1).withTitle("Antoine's Bistro").withCategory(RestaurantCategory.TRADITIONAL)
                    .withMenu(new TestDataBuilder.MenuBuilder().withTitle("Main Courses").withItem(steak).withItem(soup).build(null))
                    .withAverageRating(4.7).isApproved().isOpen().build();
            ((Owner) seller1).setRestaurant(bistro);

            Restaurant cafe = new TestDataBuilder.RestaurantBuilder()
                    .withOwner((Owner) seller2).withTitle("The Reading Nook Cafe").withCategory(RestaurantCategory.CAFE)
                    .withMenu(new TestDataBuilder.MenuBuilder().withTitle("Beverages & Snacks").withItem(latte).withItem(scone).build(null))
                    .withAverageRating(4.9).isApproved().isOpen().build();
            ((Owner) seller2).setRestaurant(cafe);

            Restaurant seafoodShack = new TestDataBuilder.RestaurantBuilder()
                    .withOwner((Owner) seller3).withTitle("The Salty Squid").withCategory(RestaurantCategory.SEAFOOD)
                    .withMenu(new TestDataBuilder.MenuBuilder().withTitle("Catch of the Day").withItem(fishAndChips).withItem(clamChowder).build(null))
                    .withAverageRating(4.2).isApproved().isOpen().build();
            ((Owner) seller3).setRestaurant(seafoodShack);

            // --- SAVE TO DB ---
            userDao.save(seller1);
            userDao.save(seller2);
            userDao.save(seller3);
            userDao.save(buyer1);
            userDao.save(courier1);
            userDao.save(admin1);

            System.out.println("\nDatabase seeding complete!");

        } catch (Exception e) {
            System.err.println("An error occurred during database seeding:");
            e.printStackTrace();
        } finally {
            JpaUtil.closeEntityManagerFactory();
        }
    }
}