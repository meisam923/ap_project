package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Review;
import util.JpaUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RatingDao implements IDao<Review, Long> {

    @Override
    public void save(Review entity) {
        executeInTransaction(em -> em.persist(entity));
    }

    @Override
    public Review findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Review.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<Review> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Review r", Review.class).getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(Review entity) {
        executeInTransaction(em -> em.merge(entity));
    }

    @Override
    public void deleteById(Long id) {
        executeInTransaction(em -> {
            Review review = em.find(Review.class, id);
            if (review != null) {
                em.remove(review);
            }
        });
    }

    @Override
    public void delete(Review entity) {
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

    public Optional<Review> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Review> query = em.createQuery(
                    "SELECT r FROM Review r WHERE r.order.id = :orderId", Review.class);
            query.setParameter("orderId", orderId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Review> findReviewsByItemId(Integer itemId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT r FROM Review r JOIN r.order o JOIN o.items oi WHERE oi.itemId = :itemId ORDER BY r.createdAt DESC";
            TypedQuery<Review> query = em.createQuery(jpql, Review.class);
            query.setParameter("itemId", itemId);
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

    public List<Review> findAllByRestaurantId(int restaurantId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT r FROM Review r WHERE r.order.restaurant.id = :restaurantId";
            TypedQuery<Review> query = em.createQuery(jpql, Review.class);
            query.setParameter("restaurantId", restaurantId);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }
}