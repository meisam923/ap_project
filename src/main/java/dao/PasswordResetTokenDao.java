package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.PasswordResetToken;
import util.JpaUtil;

import java.util.List;
import java.util.function.Consumer;

public class PasswordResetTokenDao implements IDao<PasswordResetToken, Long> {

    @Override
    public void save(PasswordResetToken entity) {
        executeInTransaction(em -> em.persist(entity));
    }

    @Override
    public PasswordResetToken findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(PasswordResetToken.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<PasswordResetToken> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<PasswordResetToken> query = em.createQuery("SELECT t FROM PasswordResetToken t", PasswordResetToken.class);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(PasswordResetToken entity) {
        executeInTransaction(em -> em.merge(entity));
    }

    @Override
    public void deleteById(Long id) {
        executeInTransaction(em -> {
            PasswordResetToken token = em.find(PasswordResetToken.class, id);
            if (token != null) {
                em.remove(token);
            }
        });
    }

    @Override
    public void delete(PasswordResetToken entity) {
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

    public PasswordResetToken findByCode(String code) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<PasswordResetToken> query = em.createQuery("SELECT t FROM PasswordResetToken t WHERE t.code = :code", PasswordResetToken.class);
            query.setParameter("code", code);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
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