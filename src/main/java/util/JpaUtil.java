package util;

import jakarta.persistence.*;

public class JpaUtil {

    private static EntityManagerFactory emf;
    private static final String PERSISTENCE_UNIT_NAME = "MyPU";

    static {
        try {
            System.out.println("JpaUtil: Attempting to create EntityManagerFactory for PU: " + PERSISTENCE_UNIT_NAME);
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
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