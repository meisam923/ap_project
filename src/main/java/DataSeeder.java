
import dao.CustomerDao;
import dao.DeliverymanDao;
import dao.OwnerDao;
import enums.ApprovalStatus;
import enums.OperationalStatus;
import enums.RestaurantCategory;
import model.*;
import util.JpaUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A standalone class to populate the database with a complete set of test data.
 * Run the main method once to seed the database.
 */
public class DataSeeder {

    private static final OwnerDao ownerDao = new OwnerDao();
    private static final CustomerDao customerDao = new CustomerDao();
    private static final DeliverymanDao deliverymanDao = new DeliverymanDao();

    public static void main(String[] args) {
        // Ensure the EntityManagerFactory is initialized before we start
        try {
            Class.forName("util.JpaUtil");
            System.out.println("JPA Initialized for Seeding.");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find JpaUtil. Make sure it's in the classpath.");
            e.printStackTrace();
            return;
        }

        System.out.println("Seeding database with initial data for order testing...");

        try {
            // --- 1. Create a Seller and their Restaurant ---
            createAndSaveOwnerWithRestaurant(
                    "Chef Antoine", "09888888888", "antoine@bistro.com",
                    "Antoine's Bistro", RestaurantCategory.TRADITIONAL, 4.7
            );

            // --- 2. Create a Buyer ---
            Customer customer = new Customer(
                    "Test Buyer Emily", "456 Customer Way", "09112223344", "emily@example.com",
                    "password123", null, "Personal Bank", "ACC123"
            );
            customerDao.save(customer);
            System.out.println("-> Created Buyer: Test Buyer Emily");

            // --- 3. Create a Courier ---
            Deliveryman deliveryman = new Deliveryman(
                    "Speedy Dave", "789 Delivery Drive", "09333334444", "dave@delivery.com",
                    "password123", null, "Courier Credit", "CRD456"
            );
            deliverymanDao.save(deliveryman);
            System.out.println("-> Created Courier: Speedy Dave");

            System.out.println("\nDatabase seeding complete! You can now start your main server.");

        } catch (Exception e) {
            System.err.println("An error occurred during database seeding:");
            e.printStackTrace();
        } finally {
            // Close the factory to terminate the seeder application
            JpaUtil.closeEntityManagerFactory();
        }
    }

    private static void createAndSaveOwnerWithRestaurant(
            String ownerName, String ownerPhone, String ownerEmail,
            String restaurantTitle, RestaurantCategory restaurantCategory, double rating) throws Exception {

        Owner owner = new Owner(ownerName, "Restaurant Address for " + ownerName, ownerPhone, ownerEmail, "password123", null, "Default Bank", "Default Account");

        Restaurant restaurant = new Restaurant(restaurantTitle, owner.getAddress(), "09" + ownerPhone.substring(2), owner, 10, 5, null);
        restaurant.setCategory(restaurantCategory);
        restaurant.setApprovalStatus(ApprovalStatus.REGISTERED); // Approved
        restaurant.setOperationalStatus(OperationalStatus.OPEN);     // Open for business
        restaurant.setAverageRating(rating);

        owner.setRestaurant(restaurant); // This link is crucial

        Menu mainMenu = new Menu(restaurant, "Main Menu");

        Item item1 = new Item("Steak Frites", "Classic steak and fries.", 25, 50, new ArrayList<>(List.of("beef", "main", "classic")), null);
        item1.addToMenu(mainMenu);
        mainMenu.addItem(item1);

        Item item2 = new Item("French Onion Soup", "Rich and savory onion soup.", 9, 100, new ArrayList<>(List.of("soup", "starter")), null);
        item2.addToMenu(mainMenu);
        mainMenu.addItem(item2);

        restaurant.addMenu(mainMenu);

        // Saving the owner will cascade and save the restaurant, menu, and items
        ownerDao.save(owner);
        System.out.println("-> Created Seller: " + ownerName + " and Restaurant: " + restaurantTitle);
    }
}