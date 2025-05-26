package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.RefreshToken;
import model.User;
import util.JpaUtil;

import java.util.List;
import java.util.Optional;

public class RefreshTokenDao implements IRefreshTokenDao {

    @Override
    public void save(RefreshToken token) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            em.persist(token);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving refresh token", e);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public RefreshToken findById(Long id) { // Returns RefreshToken (nullable) to match IDao
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(RefreshToken.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<RefreshToken> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RefreshToken> query = em.createQuery("SELECT r FROM RefreshToken r", RefreshToken.class);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(RefreshToken token) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            em.merge(token);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating refresh token", e);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            RefreshToken token = em.find(RefreshToken.class, id); // Use local em
            if (token != null) {
                em.remove(token);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting refresh token by ID", e);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void delete(RefreshToken token) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            if (!em.contains(token)) {
                token = em.merge(token);
            }
            em.remove(token);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting refresh token", e);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(RefreshToken.class, id) != null;
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public RefreshToken findByToken(String tokenValue) { // Returns RefreshToken (nullable) to match IRefreshTokenDao
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RefreshToken> query = em.createQuery(
                    "SELECT r FROM RefreshToken r WHERE r.token = :tokenValue", RefreshToken.class);
            query.setParameter("tokenValue", tokenValue);
            List<RefreshToken> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (NoResultException e) {
            return null;
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void deleteByUser(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            TypedQuery<RefreshToken> query = em.createQuery(
                    "SELECT r FROM RefreshToken r WHERE r.user = :userValue", RefreshToken.class);
            query.setParameter("userValue", user);
            List<RefreshToken> tokens = query.getResultList();
            for (RefreshToken token : tokens) {
                em.remove(token);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting refresh tokens by user", e);
        } finally {
            if (em != null) em.close();
        }
    }

    public Optional<RefreshToken> findByTokenString(String tokenString) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<RefreshToken> query = em.createQuery(
                    "SELECT r FROM RefreshToken r WHERE r.token = :tokenStringValue", RefreshToken.class);
            query.setParameter("tokenStringValue", tokenString);
            List<RefreshToken> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em != null) em.close();
        }
    }
}