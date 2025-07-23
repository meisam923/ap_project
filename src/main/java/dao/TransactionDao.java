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

    public List<Transaction> findHistoryForAdmin(String searchFilter) throws Exception {
        EntityManager em = null;
        try {
            em = JpaUtil.getEntityManager();

            // Eagerly fetch related data to prevent crashes
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT t FROM Transaction t " +
                    "LEFT JOIN FETCH t.user " +
                    "LEFT JOIN FETCH t.order ");

            // Use a single search filter with OR conditions
            if (searchFilter != null && !searchFilter.isBlank()) {
                jpql.append("WHERE LOWER(t.user.fullName) LIKE LOWER(:searchFilter) ");
                jpql.append("OR LOWER(CAST(t.method AS string)) LIKE LOWER(:searchFilter) ");
                jpql.append("OR LOWER(CAST(t.status AS string)) LIKE LOWER(:searchFilter) ");
            }

            jpql.append(" ORDER BY t.createdAt DESC");

            TypedQuery<Transaction> query = em.createQuery(jpql.toString(), Transaction.class);

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

}