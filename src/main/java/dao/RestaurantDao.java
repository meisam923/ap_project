package dao;

import jakarta.persistence.*;
import model.Restaurant;
import util.JpaUtil;

public class RestaurantDao {

    public void save(Restaurant restaurant) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Restaurant findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        Restaurant restaurant = null;
        try {
            restaurant = em.find(Restaurant.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return restaurant;
    }

    public void update(Restaurant restaurant) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Restaurant restaurant = em.find(Restaurant.class, id);
            if (restaurant != null) {
                em.remove(restaurant);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    public Restaurant findByOwnerId(Long ownerId) {
        EntityManager em = JpaUtil.getEntityManager();
        Restaurant restaurant = null;
        try {
            TypedQuery<Restaurant> query = em.createQuery(
                    "SELECT r FROM Restaurant r WHERE r.owner.id = :ownerId", Restaurant.class);
            query.setParameter("ownerId", ownerId);
            restaurant = query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("No restaurant found for owner id: " + ownerId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return restaurant;
    }
}
