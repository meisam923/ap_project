
import dao.CustomerDao;
import dao.DeliverymanDao;
import dao.OwnerDao;
import enums.RestaurantCategory;
import enums.Role;
import model.*;
import util.JpaUtil;
import util.TestDataBuilder;

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
            System.out.println("Creating users...");
            Owner seller1 = (Owner) new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Chef Antoine").withPhone("09888888888").withEmail("antoine@bistro.com").withBankInfo("Bistro Bank", "ACC1").build();
            Owner seller2 = (Owner) new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Jane Austen").withPhone("09888888882").withEmail("jane@cafe.com").withBankInfo("Cafe Bank", "ACC2").build();
            Owner seller3 = (Owner) new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Captain Ahab").withPhone("09888888884").withEmail("ahab@squid.com").withBankInfo("Whaler's Credit", "ACC3").build();
            Owner seller4 = (Owner) new TestDataBuilder.UserBuilder().withRole(Role.SELLER).withName("Al Paca").withPhone("09888888886").withEmail("al@burrito.com").withBankInfo("Farm Bank", "ACC4").build();

            Customer buyer1 = (Customer) new TestDataBuilder.UserBuilder().withRole(Role.BUYER).withName("Test Buyer Emily").withPhone("09112223344").withEmail("emily@example.com").build();
            Customer buyer2 = (Customer) new TestDataBuilder.UserBuilder().withRole(Role.BUYER).withName("John Doe").withPhone("09112223355").withEmail("john@example.com").build();

            Deliveryman courier1 = (Deliveryman) new TestDataBuilder.UserBuilder().withRole(Role.COURIER).withName("Speedy Dave").withPhone("09333334444").withEmail("dave@delivery.com").withBankInfo("Courier Credit", "ACC5").build();
            User admin1 = new TestDataBuilder.UserBuilder().withRole(Role.ADMIN).withName("Site Admin").withPhone("09000000000").withEmail("admin@app.com").build();

            // --- ITEMS ---
            System.out.println("Creating items...");
            Item steak = new TestDataBuilder.ItemBuilder().withTitle("Steak Frites").withPrice(25).withStock(50).withKeywords(List.of("beef", "classic")).build();
            Item soup = new TestDataBuilder.ItemBuilder().withTitle("French Onion Soup").withPrice(9).withStock(100).withKeywords(List.of("soup", "starter")).build();
            Item latte = new TestDataBuilder.ItemBuilder().withTitle("Latte").withPrice(5).withStock(200).withKeywords(List.of("coffee", "drink")).build();
            Item scone = new TestDataBuilder.ItemBuilder().withTitle("Scone").withPrice(4).withStock(80).withKeywords(List.of("pastry", "snack")).build();
            Item fishAndChips = new TestDataBuilder.ItemBuilder().withTitle("Fish and Chips").withPrice(18).withStock(40).withKeywords(List.of("fried", "seafood")).build();
            Item clamChowder = new TestDataBuilder.ItemBuilder().withTitle("Clam Chowder").withPrice(9).withStock(60).withKeywords(List.of("soup", "seafood")).build();
            Item burrito = new TestDataBuilder.ItemBuilder().withTitle("The Classic Burrito").withPrice(12).withStock(150).withKeywords(List.of("breakfast", "spicy")).build();

            // --- RESTAURANTS & MENUS ---
            System.out.println("Creating restaurants and menus...");
            Restaurant bistro = new TestDataBuilder.RestaurantBuilder().withOwner(seller1).withTitle("Antoine's Bistro").withCategory(RestaurantCategory.TRADITIONAL).withAverageRating(4.7).isApproved().isOpen().build();
            Menu bistroMenu = new TestDataBuilder.MenuBuilder().withTitle("Main Menu").withItem(steak).withItem(soup).build(bistro);
            bistro.addMenu(bistroMenu);
            seller1.setRestaurant(bistro);

            Restaurant cafe = new TestDataBuilder.RestaurantBuilder().withOwner(seller2).withTitle("The Reading Nook Cafe").withCategory(RestaurantCategory.CAFE).withAverageRating(4.9).isApproved().isOpen().build();
            Menu cafeMenu = new TestDataBuilder.MenuBuilder().withTitle("Beverages & Snacks").withItem(latte).withItem(scone).build(cafe);
            cafe.addMenu(cafeMenu);
            seller2.setRestaurant(cafe);

            Restaurant seafoodShack = new TestDataBuilder.RestaurantBuilder().withOwner(seller3).withTitle("The Salty Squid").withCategory(RestaurantCategory.SEAFOOD).withAverageRating(4.2).isApproved().isOpen().build();
            Menu seafoodMenu = new TestDataBuilder.MenuBuilder().withTitle("Catch of the Day").withItem(fishAndChips).withItem(clamChowder).build(seafoodShack);
            seafoodShack.addMenu(seafoodMenu);
            seller3.setRestaurant(seafoodShack);

            Restaurant burritoBarn = new TestDataBuilder.RestaurantBuilder().withOwner(seller4).withTitle("The Burrito Barn").withCategory(RestaurantCategory.BREAKFAST).withAverageRating(4.8).build(); // Stays in WAITING status
            Menu burritoMenu = new TestDataBuilder.MenuBuilder().withTitle("Breakfast").withItem(burrito).build(burritoBarn);
            burritoBarn.addMenu(burritoMenu);
            seller4.setRestaurant(burritoBarn);

            // --- SAVE TO DB ---
            System.out.println("Saving all entities to the database...");
            ownerDao.save(seller1);
            ownerDao.save(seller2);
            ownerDao.save(seller3);
            ownerDao.save(seller4);
            customerDao.save(buyer1);
            customerDao.save(buyer2);
            deliverymanDao.save(courier1);
            customerDao.save((Customer) admin1); // Save the admin (as a Customer type for simplicity)

            System.out.println("\nDatabase seeding complete!");

        } catch (Exception e) {
            System.err.println("An error occurred during database seeding:");
            e.printStackTrace();
        } finally {
            JpaUtil.closeEntityManagerFactory();
        }
    }
}