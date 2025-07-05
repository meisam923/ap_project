package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Transaction;
import util.JpaUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
}