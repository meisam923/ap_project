package dao;

import enums.ApprovalStatus;
import enums.OperationalStatus;
import jakarta.persistence.*;
import model.Restaurant;
import util.JpaUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantDao {

    public void save(Restaurant restaurant) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(restaurant);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            em.close();
        }
    }

    public Restaurant findById(Long id) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        Restaurant restaurant = null;
        try {
            restaurant = em.find(Restaurant.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            em.close();
        }
        return restaurant;
    }

    public void update(Restaurant restaurant) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(restaurant);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } {
            em.close();
        }
    }

    public void deleteById(Long id)throws Exception {
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
            throw e;
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

    public Restaurant findByPhone(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        try{
            TypedQuery<Restaurant> query = em.createQuery("SELECT c FROM Restaurant c WHERE c.phoneNumber = :phone", Restaurant.class);
            query.setParameter("phone", phone);
            return query.getSingleResult();
        }  catch (NoResultException e) {
            return null;
        }finally {
            em.close();
        }
    }

    public List<Restaurant> findVendors(String searchTerm, List<String> keywords, ApprovalStatus approvalStatus, OperationalStatus operationalStatus) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpqlBuilder = new StringBuilder("SELECT DISTINCT r FROM Restaurant r WHERE 1=1");
            Map<String, Object> parameters = new HashMap<>();

            // Filter by APPROVAL status (e.g., only show REGISTERED restaurants)
            if (approvalStatus != null) {
                jpqlBuilder.append(" AND r.approvalStatus = :approvalStatus");
                parameters.put("approvalStatus", approvalStatus);
            }

            // Filter by OPERATIONAL status (e.g., only show OPEN restaurants)
            if (operationalStatus != null) {
                jpqlBuilder.append(" AND r.operationalStatus = :operationalStatus");
                parameters.put("operationalStatus", operationalStatus);
            }

            // Filter by search keyword on the restaurant title
            if (searchTerm != null && !searchTerm.isBlank()) {
                jpqlBuilder.append(" AND LOWER(r.title) LIKE :searchTerm");
                parameters.put("searchTerm", "%" + searchTerm.toLowerCase() + "%");
            }

            // Filter by keyword array matching on title
            if (keywords != null && !keywords.isEmpty()) {
                jpqlBuilder.append(" AND (");
                for (int i = 0; i < keywords.size(); i++) {
                    if (i > 0) {
                        jpqlBuilder.append(" OR");
                    }
                    String paramName = "keyword" + i;
                    jpqlBuilder.append(" LOWER(r.title) LIKE :").append(paramName);
                    parameters.put(paramName, "%" + keywords.get(i).toLowerCase() + "%");
                }
                jpqlBuilder.append(" )");
            }

            jpqlBuilder.append(" ORDER BY r.title ASC");

            TypedQuery<Restaurant> query = em.createQuery(jpqlBuilder.toString(), Restaurant.class);
            parameters.forEach(query::setParameter);

            return query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) em.close();
        }
    }
}
