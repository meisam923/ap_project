import jakarta.persistence.*;
import model.*;

public class Main {
    public static void main(String[] args) {
        // 1) Bootstrap JPA
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyPU");
        EntityManager em = emf.createEntityManager();

        try {
            // 2) Begin a transaction
            em.getTransaction().begin();

            // 3) Create & persist a Customer
            Customer customer = new Customer(
                    "Ali", "Rezai",
                    "09121234567",
                    "ali@gmail.com",
                    "my_password",
                    new Address("1231","131"),
                    new Location(123,123)
            );
            customer.setLocation(new Location(35.7, 51.4));
            customer.setAddress(new Address("Tehran12345", "Home"));
            em.persist(customer);

            // 4) Create & persist an Owner
            Restaurant sampleRestaurant = new Restaurant();  // assume it's an @Entity too
            em.persist(sampleRestaurant);

            Owner owner = new Owner(
                    "Sara", "Ahmadi",
                    "09351234567",
                    "rezaj123rezaj123@gmail.com",
                    "owner_pass",
                    new Address("1231","131"),
                    new Location(123,123)
            );
            owner.setLocation(new Location(35.8, 51.5));
            owner.setAddress(new Address("Street", "54321"));
            owner.setRestaurant(sampleRestaurant);
            em.persist(owner);

            // 5) Commit — Hibernate will auto-update your schema and insert rows
            em.getTransaction().commit();

            // 6) Query back all users to verify
            System.out.println("\nAll Users in DB:");
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
            for (User u : q.getResultList()) {
                System.out.printf("- %s: %s %s (%s)%n",
                        u.getRole(), u.getFirstName(), u.getLastName(), u.getEmail());
            }
        } finally {
            // 7) Clean up
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            emf.close();
        }
    }
}
