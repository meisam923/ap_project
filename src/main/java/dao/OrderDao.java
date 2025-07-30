package dao;

import enums.OrderRestaurantStatus;
import enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Order;
import util.JpaUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT o FROM Order o " +
                            "LEFT JOIN FETCH o.items oi " +
                            "LEFT JOIN FETCH o.restaurant r " +
                            "WHERE o.customer.id = :customerId"
            );

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("customerId", customerId);

            if (vendorName != null && !vendorName.isBlank()) {
                jpql.append(" AND LOWER(r.title) LIKE LOWER(:vendorName)");
                parameters.put("vendorName", "%" + vendorName + "%");
            }

            if (searchKeyword != null && !searchKeyword.isBlank()) {
                jpql.append(" AND LOWER(oi.itemName) LIKE LOWER(:searchKeyword)");
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


    public List<Order> findHistoryForAdmin(String searchFilter) throws Exception {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();

            StringBuilder jpql = new StringBuilder("SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.customer c " +
                    "LEFT JOIN FETCH o.restaurant r " +
                    "LEFT JOIN FETCH o.items oi ");

            if (searchFilter != null && !searchFilter.isBlank()) {
                jpql.append("WHERE LOWER(c.fullName) LIKE LOWER(:searchFilter) ");
                jpql.append("OR LOWER(r.title) LIKE LOWER(:searchFilter) ");
                jpql.append("OR LOWER(oi.itemName) LIKE LOWER(:searchFilter) ");
            }

            jpql.append(" ORDER BY o.createdAt DESC");

            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);

            if (searchFilter != null && !searchFilter.isBlank()) {
                query.setParameter("searchFilter", "%" + searchFilter + "%");
            }

            return query.getResultList();
        } finally {
            if (em != null) {
                em.close();
            }
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

    public List<Order> findAvailableForDelivery() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT DISTINCT o FROM Order o " +
                            "JOIN FETCH o.restaurant " +
                            "JOIN FETCH o.items " +
                            "WHERE o.status = :status AND o.deliveryman IS NULL",
                    Order.class
            );
            query.setParameter("status", OrderStatus.FINDING_COURIER);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Order> findHistoryForCourier(Long courierId, String searchFilter, String vendorFilter) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.deliveryman.id = :courierId");
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("courierId", courierId);

            if (vendorFilter != null && !vendorFilter.isBlank()) {
                jpql.append(" AND LOWER(o.restaurant.title) LIKE LOWER(:vendorName)");
                parameters.put("vendorName", "%" + vendorFilter + "%");
            }

            if (searchFilter != null && !searchFilter.isBlank()) {
                jpql.append(" AND EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = o AND LOWER(oi.itemName) LIKE LOWER(:searchKeyword))");
                parameters.put("searchKeyword", "%" + searchFilter + "%");
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
    public List<Object[]> getMonthlyIncomeForRestaurant(int restaurantId) throws Exception {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();

            String jpql = """
            SELECT 
                EXTRACT(YEAR FROM o.createdAt) AS year,
                EXTRACT(MONTH FROM o.createdAt) AS month,
                SUM(o.totalPrice) AS totalIncome
            FROM Order o
            WHERE o.restaurant.id = :restaurantId
              AND o.status = :completedStatus
            GROUP BY EXTRACT(YEAR FROM o.createdAt), EXTRACT(MONTH FROM o.createdAt)
            ORDER BY EXTRACT(YEAR FROM o.createdAt) DESC, EXTRACT(MONTH FROM o.createdAt) DESC
            """;


            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("restaurantId", restaurantId);
            query.setParameter("completedStatus", OrderStatus.COMPLETED);
            return query.getResultList();  // each Object[]: [year, month, totalIncome]
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    public List<Order> findHistoryForRestaurant(int restaurantId, HashMap<String, String> queryFilters) throws Exception {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();

            StringBuilder jpql = new StringBuilder(
                    "SELECT DISTINCT o FROM Order o " +
                            "LEFT JOIN FETCH o.customer c " +
                            "LEFT JOIN FETCH o.restaurant r " +
                            "LEFT JOIN FETCH o.deliveryman d " +
                            "LEFT JOIN FETCH o.items oi " +
                            "LEFT JOIN FETCH o.review rv " +
                            "LEFT JOIN FETCH rv.imagesBase64 img "
            );

            List<String> conditions = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();

            conditions.add("o.restaurant.id = :restaurantId");
            parameters.put("restaurantId", restaurantId);

            if (queryFilters != null && queryFilters.containsKey("status") && !queryFilters.get("status").isEmpty()) {
                conditions.add("o.restaurantStatus = :status");
                parameters.put("status", OrderRestaurantStatus.fromString(queryFilters.get("status")));
            }

            if (queryFilters != null && queryFilters.containsKey("search") && !queryFilters.get("search").isEmpty()) {
                String searchFilter = "%" + queryFilters.get("search").toLowerCase() + "%";
                conditions.add("(" +
                        "LOWER(c.fullName) LIKE :searchFilter " +
                        "OR LOWER(d.fullName) LIKE :searchFilter " +
                        "OR LOWER(oi.itemName) LIKE :searchFilter" +
                        ")");
                parameters.put("searchFilter", searchFilter);
            }

            if (!conditions.isEmpty()) {
                jpql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            jpql.append(" ORDER BY o.createdAt DESC");

            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);
            parameters.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}