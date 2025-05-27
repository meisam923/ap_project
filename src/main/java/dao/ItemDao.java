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
    public Item findById(int l) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        Item item = null;
        try {
            item = em.find(Item.class, l);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return item;
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
    public void delete(Item item) throws Exception {
        EntityManager em= JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            em.remove(item);
            tx.commit();
        }
        finally {
            em.close();
        }
    }
}
