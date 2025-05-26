package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import model.RefreshToken;
import model.User;
import util.JpaUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RefreshTokenDao implements IRefreshTokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(RefreshToken token) {
        em.persist(token);
    }

    @Override
    public RefreshToken findById(Long id) {
        return em.find(RefreshToken.class, id);
    }

    @Override
    public List<RefreshToken> getAll() {
        TypedQuery<RefreshToken> query = em.createQuery("SELECT r FROM RefreshToken r", RefreshToken.class);
        return query.getResultList();
    }

    @Override
    public void update(RefreshToken token) {
        em.merge(token);
    }

    @Override
    public void deleteById(Long id) {
        RefreshToken token = findById(id);
        if (token != null) {
            delete(token);
        }
    }

    @Override
    public void delete(RefreshToken token) {
        if (!em.contains(token)) {
            token = em.merge(token);
        }
        em.remove(token);
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id) != null;
    }

    @Override
    public RefreshToken findByToken(String token) {
        TypedQuery<RefreshToken> query = em.createQuery(
                "SELECT r FROM RefreshToken r WHERE r.token = :token", RefreshToken.class);
        query.setParameter("token", token);

        List<RefreshToken> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void deleteByUser(User user) {
        TypedQuery<RefreshToken> query = em.createQuery(
                "SELECT r FROM RefreshToken r WHERE r.user = :user", RefreshToken.class);
        query.setParameter("user", user);

        List<RefreshToken> tokens = query.getResultList();
        for (RefreshToken token : tokens) {
            delete(token);
        }
    }

    public Optional<RefreshToken> findByTokenString(String tokenString) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Ensure your RefreshToken entity has a field named 'token' mapped to the token string
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
