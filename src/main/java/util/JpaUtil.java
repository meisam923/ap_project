package util;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static EntityManagerFactory emf;
    private static final String PERSISTENCE_UNIT_NAME = "MyPU";

    static {
        try {
            System.out.println("JpaUtil: Attempting to create EntityManagerFactory for PU: " + PERSISTENCE_UNIT_NAME);
            Map<String, String> properties = new HashMap<>();

            // Read database connection details from environment variables
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");

            // --- IMPORTANT ---
            // Use sensible defaults if the environment variables are not set.
            // This allows the app to still run on your local machine.
            if (dbUrl == null) {
                dbUrl = "jdbc:postgresql://localhost:5432/postgres"; // Your local URL
            }
            if (dbUser == null) {
                dbUser = "postgres"; // or postgers
            }
            if (dbPassword == null) {
                dbPassword = "newpassword"; // Your local password
            }

            // Set the properties for JPA to use
            properties.put("jakarta.persistence.jdbc.url", dbUrl);
            properties.put("jakarta.persistence.jdbc.user", dbUser);
            properties.put("jakarta.persistence.jdbc.password", dbPassword);

            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
            System.out.println("JpaUtil: EntityManagerFactory created successfully: " + (emf != null && emf.isOpen()));
        } catch (Throwable ex) {
            System.err.println("JpaUtil: FATAL - Initial EntityManagerFactory creation failed.");
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            System.err.println("JpaUtil: EntityManagerFactory is null. This should not happen after static initialization.");
            throw new IllegalStateException("EntityManagerFactory is null. Initialization failed.");
        }
        if (!emf.isOpen()) {
            System.err.println("JpaUtil: EntityManagerFactory is closed! Cannot create EntityManager.");
            throw new IllegalStateException("EntityManagerFactory is closed. It was likely closed prematurely.");
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            System.out.println("JpaUtil: Closing EntityManagerFactory...");
            emf.close();
            System.out.println("JpaUtil: EntityManagerFactory closed.");
        } else {
            System.out.println("JpaUtil: EntityManagerFactory already null or closed.");
        }
    }
}