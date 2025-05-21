import jakarta.persistence.*;
import model.*;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyPU");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Customer customer = new Customer(
                    "Ali", "Rezai",
                    "09121234561117",
                    "alaaaa@gmail.com",
                    "my_password",
                    new Address("1231", "131"),
                    new Location(123, 123)
            );
            customer.setLocation(new Location(35.7, 51.4));
            customer.setAddress(new Address("Tehran12345", "Home"));
            em.merge(customer);

            Restaurant sampleRestaurant = new Restaurant();
            em.merge(sampleRestaurant);

            Owner owner = new Owner(
                    "Sara", "Ahmadi",
                    "0933351234567",
                    "rezaaaaj123rezaj123@gmail.com",
                    "owner_pass",
                    new Address("1231", "131"),
                    new Location(123, 123)
            );
            owner.setLocation(new Location(35.8, 51.5));
            owner.setAddress(new Address("Street", "54321"));
            owner.setRestaurant(sampleRestaurant);
            em.merge(owner);

            em.getTransaction().commit();

            System.out.println("\nAll Users in DB:");
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
            for (User u : q.getResultList()) {
                System.out.printf("- %s: %s %s (%s)%n",
                        u.getRole(), u.getFirstName(), u.getLastName(), u.getEmail());
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            emf.close();
        }


    }
}

