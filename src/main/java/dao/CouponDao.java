package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Coupon;
import util.JpaUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CouponDao implements IDao<Coupon, Integer> {

    @Override
    public void save(Coupon coupon) {
        executeInTransaction(em -> em.persist(coupon));
    }

    @Override
    public Coupon findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Coupon.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<Coupon> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Coupon> query = em.createQuery("SELECT c FROM Coupon c", Coupon.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(Coupon coupon) {
        executeInTransaction(em -> em.merge(coupon));
    }

    @Override
    public void deleteById(Integer id) {
        executeInTransaction(em -> {
            Coupon coupon = em.find(Coupon.class, id);
            if (coupon != null) {
                em.remove(coupon);
            }
        });
    }

    @Override
    public void delete(Coupon coupon) {
        executeInTransaction(em -> {
            if (!em.contains(coupon)) {
                em.remove(em.merge(coupon));
            } else {
                em.remove(coupon);
            }
        });
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id) != null;
    }

    public Optional<Coupon> findByCode(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Coupon> query = em.createQuery(
                    "SELECT c FROM Coupon c WHERE c.couponCode = :code", Coupon.class);
            query.setParameter("code", couponCode);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
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
            if (tx != null && tx.isActive()){ tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Database transaction failed", e);}
        } finally {
            if (em != null) em.close();
        }
    }
}
