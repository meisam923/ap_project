package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Item;
import model.Price; // Assuming Price embeddable is in model package
import util.JpaUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDao {
    public void save(Item item) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(item);
            tx.commit();
        } finally {
            em.close();
        }
    }

    public Item findById(long id) throws Exception {
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
            em.close();
        }
    }

    public void update(Item item) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(item);
            tx.commit();
        } finally {
            em.close();
        }
    }

    public void delete(int itemId) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Item item = em.find(Item.class, itemId);
            if (item != null) {
                em.remove(item);
            }
            tx.commit();
        } finally {
            em.close();
        }
    }

    public List<Item> findItems(String searchTerm, Integer maxPrice, List<String> keywords) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpqlBuilder = new StringBuilder("SELECT DISTINCT i FROM Item i LEFT JOIN i.hashtags h WHERE 1=1");
            Map<String, Object> params = new HashMap<>();

            // Filter by search term
            if (searchTerm != null && !searchTerm.isBlank()) {
                jpqlBuilder.append(" AND LOWER(i.title) LIKE LOWER(:searchTerm)");
                params.put("searchTerm", "%" + searchTerm + "%");
            }

            // Filter by maximum price
            if (maxPrice != null && maxPrice > 0) {
                jpqlBuilder.append(" AND i.price.price <= :maxPrice");
                params.put("maxPrice", new BigDecimal(maxPrice));
            }

            // Filter by keywords in the item's hashtags
            if (keywords != null && !keywords.isEmpty()) {
                jpqlBuilder.append(" AND h IN :keywords");
                params.put("keywords", keywords);
            }

            jpqlBuilder.append(" ORDER BY i.price.price ASC");

            TypedQuery<Item> query = em.createQuery(jpqlBuilder.toString(), Item.class);
            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }
}