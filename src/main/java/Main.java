import Handler.AuthHandler;
import Handler.ItemHandler;
import Handler.OrderHandler; // Import the new OrderHandler
import Handler.RestaurantHandler;
import Handler.VendorHandler;
import com.sun.net.httpserver.HttpServer;
import util.JpaUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        // --- 1. Initialize Database Connection (JPA) ---
        try {
            System.out.println("Main: Triggering JpaUtil static initializer...");
            Class.forName("util.JpaUtil"); // Ensures the static block in JpaUtil runs
            if (JpaUtil.getEntityManagerFactory() == null || !JpaUtil.getEntityManagerFactory().isOpen()) {
                System.err.println("Main: EntityManagerFactory is not available after initialization. Exiting.");
                return;
            }
            System.out.println("Main: JpaUtil static initializer completed successfully.");
        } catch (Throwable e) {
            System.err.println("Main: CRITICAL ERROR during JpaUtil/EMF initialization. Server cannot start.");
            e.printStackTrace();
            return;
        }

        // --- 2. Add a Shutdown Hook ---
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Main: Shutdown hook triggered. Closing resources...");
            JpaUtil.closeEntityManagerFactory();
            System.out.println("Main: Resources closed. Exiting.");
        }));

        // --- 3. Create and Configure HTTP Server ---
        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        try {
            // Register all your handlers here
            server.createContext("/auth", new AuthHandler());           // For authentication
            server.createContext("/restaurants", new RestaurantHandler()); // For seller-facing management
            server.createContext("/vendors", new VendorHandler());         // For buyer-facing searching
            server.createContext("/items", new ItemHandler());           // For buyer-facing searching
            server.createContext("/orders", new OrderHandler());          // For buyer-facing order management

        } catch (Exception e) {
            System.err.println("Main: Error creating HTTP context handlers: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        server.setExecutor(null); // Use the default executor
        server.start();

        // --- 4. Print Startup Messages ---
        System.out.println("=====================================================");
        System.out.println("Server is running on port " + port);
        System.out.println("Auth endpoints available at       http://localhost:" + port + "/auth/*");
        System.out.println("Restaurant endpoints (seller) at  http://localhost:" + port + "/restaurants/*");
        System.out.println("Vendor endpoints (buyer) at       http://localhost:" + port + "/vendors/*");
        System.out.println("Item endpoints (buyer) at         http://localhost:" + port + "/items/*");
        System.out.println("Order endpoints (buyer) at        http://localhost:" + port + "/orders/*");
        System.out.println("=====================================================");
    }
}