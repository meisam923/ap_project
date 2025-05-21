package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Restaurant;
import model.User;
import util.JpaUtil;

public class UserDao {
    public User findUserByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT r FROM User r WHERE r.email = :email", User.class);
            query.setParameter("email", email);
            user= query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("No user found for email : " + email);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return user;
    }

    public void save(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            em.persist(user);
            tx.commit();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            em.close();
        }
    }

    public void delete(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            em.remove(user);
            tx.commit();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            em.close();
        }
    }

    public void update(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            em.merge(user);
            tx.commit();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            em.close();
        }
    }
}

