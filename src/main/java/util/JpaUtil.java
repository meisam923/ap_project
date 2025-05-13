package util;

import jakarta.persistence.*;

public class JpaUtil {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("MyPU");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
