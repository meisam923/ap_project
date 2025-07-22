// Replace the entire content of your Main.java with this version.

import Handler.*;
import com.sun.net.httpserver.HttpServer;
import util.JpaUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        // Initialize JPA
        try {
            Class.forName("util.JpaUtil");
        } catch (Throwable e) {
            System.err.println("CRITICAL ERROR: JpaUtil initialization failed. Server cannot start.");
            e.printStackTrace();
            return;
        }

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // --- THIS IS THE FIX: A GLOBAL EXCEPTION HANDLER ---
        // We are creating a custom executor for the server.
        // This wrapper will surround every single incoming request with a try-catch block.
        Executor exceptionLoggingExecutor = Executors.newFixedThreadPool(20, r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler((thread, throwable) -> {
                // This will catch any error that isn't caught elsewhere
                System.err.println("====== UNCAUGHT GLOBAL EXCEPTION ======");
                System.err.println("Error on thread: " + thread.getName());
                throwable.printStackTrace();
                System.err.println("=====================================");
            });
            return t;
        });

        server.setExecutor(exceptionLoggingExecutor);
        // --- END OF FIX ---

        // Register all your existing handlers
        server.createContext("/auth", new AuthHandler());
        server.createContext("/restaurants", new RestaurantHandler());
        server.createContext("/vendors", new VendorHandler());
        server.createContext("/items", new ItemHandler());
        server.createContext("/orders", new OrderHandler());
        server.createContext("/favorites", new FavoriteHandler());
        server.createContext("/ratings", new RatingHandler());
        server.createContext("/deliveries", new DeliveryHandler());
        server.createContext("/wallet", new PaymentHandler());
        server.createContext("/payment", new PaymentHandler());
        server.createContext("/transactions", new PaymentHandler());
        server.createContext("/admin", new AdminHandler());


        // Add a shutdown hook to close the database connection pool gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shutting down...");
            server.stop(0);
            JpaUtil.closeEntityManagerFactory();
            System.out.println("Resources closed.");
        }));

        server.start();
        System.out.println("=====================================================");
        System.out.println("Server is running on port " + port);
        System.out.println("=====================================================");
    }
}