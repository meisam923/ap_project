package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Item;
import util.JpaUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDao implements IDao<Item, Integer> {

    @Override
    public void save(Item item) {
        executeInTransaction(em -> em.persist(item));
    }

    @Override
    public Item findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery(
                            "SELECT DISTINCT i FROM Item i " +
                                    "LEFT JOIN FETCH i.menus m " +
                                    "LEFT JOIN FETCH m.restaurant " +
                                    "WHERE i.id = :id", Item.class)
                    .setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<Item> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i", Item.class);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(Item item) {
        executeInTransaction(em -> em.merge(item));
    }

    @Override
    public void deleteById(Integer id) {
        executeInTransaction(em -> {
            Item item = em.find(Item.class, id);
            if (item != null) {
                em.remove(item);
            }
        });
    }

    @Override
    public void delete(Item item) {
        executeInTransaction(em -> {
            if (!em.contains(item)) {
                em.remove(em.merge(item));
            } else {
                em.remove(item);
            }
        });
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id) != null;
    }

    public List<Item> findItems(String searchTerm, Integer maxPrice, List<String> keywords) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // --- THIS IS THE FIX ---
            // We add "LEFT JOIN FETCH i.menus m" and "LEFT JOIN FETCH m.restaurant" to the query.
            // This tells Hibernate to get all the necessary data in one single, efficient query
            // while the database session is still open, preventing the LazyInitializationException.
            StringBuilder jpqlBuilder = new StringBuilder("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.menus m LEFT JOIN FETCH m.restaurant LEFT JOIN i.hashtags h WHERE 1=1");
            // --- END OF FIX ---

            Map<String, Object> params = new HashMap<>();

            if (searchTerm != null && !searchTerm.isBlank()) {
                jpqlBuilder.append(" AND LOWER(i.title) LIKE LOWER(:searchTerm)");
                params.put("searchTerm", "%" + searchTerm + "%");
            }
            if (maxPrice != null && maxPrice > 0) {
                jpqlBuilder.append(" AND i.price.price <= :maxPrice");
                params.put("maxPrice", new BigDecimal(maxPrice));
            }
            if (keywords != null && !keywords.isEmpty()) {
                jpqlBuilder.append(" AND h IN :keywords");
                params.put("keywords", keywords);
            }
            jpqlBuilder.append(" ORDER BY i.price.price ASC");

            TypedQuery<Item> query = em.createQuery(jpqlBuilder.toString(), Item.class);
            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error searching items: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }


    private void executeInTransaction(java.util.function.Consumer<EntityManager> action) {
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