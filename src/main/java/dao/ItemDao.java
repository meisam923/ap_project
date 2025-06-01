package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Customer;
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
    public Item findById(int id) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT i FROM Item i " +
                                    "LEFT JOIN FETCH i.menus m " +
                                    "LEFT JOIN FETCH m.restaurant " +
                                    "WHERE i.id = :id", Item.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } finally {
            em.close();
        }
    }
    public void update(Item item) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            em.merge(item);
            tx.commit();
        }
        finally {
            em.close();
        }
    }
    public void delete(int itemId) throws Exception {
        EntityManager em= JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            Item item = em.find(Item.class, itemId); // attached entity
            if (item != null) {
                em.remove(item);
            }
            tx.commit();
        }
        finally {
            em.close();
        }
    }
}
