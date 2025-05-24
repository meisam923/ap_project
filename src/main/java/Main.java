import com.sun.net.httpserver.HttpServer;
import Handler.HelloHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register handler at /hello
        server.createContext("/hello", new HelloHandler());

        server.setExecutor(null); // uses default executor
        server.start();
        System.out.println("Server is running at http://localhost:" + port + "/hello");


        /*EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyPU");
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


            UserDao userDao = new UserDao();
            User us=userDao.findUserByEmail("seyedmohammadrezahamidi@gmail.com");
            RestaurantDao restaurantDao = new RestaurantDao();
            Restaurant rs=restaurantDao.findByOwnerId(((Owner)us).getId());
            rs.setPhone_number("09998765432");
            LocalTime start = LocalTime.of(12,15);
            LocalTime end = LocalTime.of(14,15);
            if (rs.setPeriod(start, end))
                System.out.println("set");
            restaurantDao.update(rs);

            Restaurant sampleRestaurant = new Restaurant (new Address("tst","tst"),new Location(90,90),"09903099157","kababi",owner, "Cafe");

           restaurantDao.save(sampleRestaurant);
            owner.setRestaurant(sampleRestaurant);
            em.merge(owner);

            em.getTransaction().commit();
            System.out.println("\nAll Users in DB:");
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u", User.class);
            for (User u : q.getResultList()) {
                System.out.printf("- %s: %s %s (%s)%n",
                        u.getRole(), u.getFullName(), u.getLastName(), u.getEmail());
            }
        } catch (NotAcceptableException e) {
            System.err.println(e.getMessage());
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            emf.close();
        }*/
    }
}

