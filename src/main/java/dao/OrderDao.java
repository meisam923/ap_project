package dao;

import enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transaction;
import model.Order;
import model.Restaurant;
import util.JpaUtil;

import java.util.List;

public class OrderDao {
    public Order findById(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        Order order = null;
        try {
            order = em.find(Order.class, id);
        }finally {
            em.close();
        }
        return order;
    }
    public void update(Order order) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            em.merge(order);
            tx.commit();
        }
        finally {
            em.close();
        }
    }
    public List<Order> findByStatus(OrderStatus status) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<Order> query = em.createQuery("SELECT o FROM Order o WHERE o.status = :status", Order.class);
            query.setParameter("status", status);

            List<Order> orders = query.getResultList();
            tx.commit();

            return orders;
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }
    }

}
