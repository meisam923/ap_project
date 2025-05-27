package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Item;
import model.Menu;
import util.JpaUtil;

public class ItemDao {
    public void save(Item item) throws Exception {
        EntityManager em= JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            em.persist(item);
            tx.commit();
        } finally {
            em.close();
        }
    }
}
