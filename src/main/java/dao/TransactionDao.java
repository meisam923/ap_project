package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Order;
import model.Transaction;
import util.JpaUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransactionDao {
    public void save(Transaction transaction) {
        executeInTransaction(em -> em.persist(transaction));
    }

    public List<Transaction> findByUser(Long userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC",
                    Transaction.class
            );
            query.setParameter("userId", userId);
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
            throw new RuntimeException("DAO transaction failed", e);
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Transaction> findHistoryForAdmin(String searchFilter, String userFilter,
                                                 String methodFilter, String statusFilter) throws Exception {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT o FROM Transaction o ");
            List<String> conditions = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();

            if (userFilter != null && !userFilter.isBlank()) {
                conditions.add("LOWER(o.user.fullName) LIKE LOWER(:userFilter)");
                parameters.put("userFilter", "%" + userFilter + "%");
            }
            if (statusFilter != null && !statusFilter.isBlank()) {
                conditions.add("LOWER(o.status) LIKE LOWER(:statusFilter)");
                parameters.put("statusFilter", "%" + statusFilter + "%");
            }
            if (methodFilter != null && !methodFilter.isBlank()) {
                conditions.add("LOWER(o.method) LIKE LOWER(:methodFilter)");
                parameters.put("methodFilter", "%" + methodFilter + "%");
            }

            if (searchFilter != null && !searchFilter.isBlank()) {
                jpql.append(" JOIN o.order ord ");
                conditions.add("EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.order = ord AND LOWER(oi.itemName) LIKE LOWER(:searchFilter))");
                parameters.put("searchFilter", "%" + searchFilter + "%");
            }

            if (!conditions.isEmpty()) {
                jpql.append(" WHERE ").append(conditions.stream().collect(Collectors.joining(" AND ")));
            }

            jpql.append(" ORDER BY o.createdAt DESC");

            TypedQuery<Transaction> query = em.createQuery(jpql.toString(), Transaction.class);
            parameters.forEach(query::setParameter);

            return query.getResultList();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

}