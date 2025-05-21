package Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.Address;
import model.Customer;
import model.Location;

public class JpaTest {
    public static void main(String[] args) {
        // Create EntityManagerFactory using the persistence unit defined in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPU");
        EntityManager em = emf.createEntityManager();

        try {
            // Begin transaction
            em.getTransaction().begin();

            // Create a new Customer (or any subclass of User)
            Customer customer = new Customer(
                    "Alice",                     // first name
                    "Smith",                     // last name
                    "1234567890",                // phone
                    "alice@example.com",         // email
                    "password123",               // password
                    new Address("123","12")    ,new Location(123,12314)

            );

            // Persist to DB
            em.persist(customer);

            // Commit transaction
            em.getTransaction().commit();

            System.out.println("Customer saved with publicId: " + customer.getPublicId());
        } finally {
            em.close();
            emf.close();
        }
    }
}
