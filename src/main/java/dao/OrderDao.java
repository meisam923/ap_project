package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Order;
import util.JpaUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OrderDao implements IDao<Order, Long> {

    @Override
    public void save(Order entity) {
        executeInTransaction(em -> em.persist(entity));
    }

    @Override
    public Order findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id", Order.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<Order> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Order> query = em.createQuery("SELECT o FROM Order o", Order.class);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(Order entity) {
        executeInTransaction(em -> em.merge(entity));
    }

    @Override
    public void deleteById(Long id) {
        executeInTransaction(em -> {
            Order order = em.find(Order.class, id);
            if (order != null) {
                em.remove(order);
            }
        });
    }

    @Override
    public void delete(Order entity) {
        executeInTransaction(em -> {
            if (!em.contains(entity)) {
                em.remove(em.merge(entity));
            } else {
                em.remove(entity);
            }
        });
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id) != null;
    }

    public List<Order> findHistoryForUser(Long customerId, String searchKeyword, String vendorName) {
        EntityManager em = JpaUtil.getEntityManager();
        try {

            StringBuilder jpql = new StringBuilder("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.customer.id = :customerId");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("customerId", customerId);

            if (vendorName != null && !vendorName.isBlank()) {
                jpql.append(" AND LOWER(o.restaurant.title) LIKE LOWER(:vendorName)");
                parameters.put("vendorName", "%" + vendorName + "%");
            }

            if (searchKeyword != null && !searchKeyword.isBlank()) {
                jpql.append(" AND EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND LOWER(oi.itemName) LIKE LOWER(:searchKeyword))");
                parameters.put("searchKeyword", "%" + searchKeyword + "%");
            }

            jpql.append(" ORDER BY o.createdAt DESC");

            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }

    private void executeInTransaction(Consumer<EntityManager> action) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Database transaction failed", e);
        } finally {
            if (em != null) em.close();
        }
    }
}