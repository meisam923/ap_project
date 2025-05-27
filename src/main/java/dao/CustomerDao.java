package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Customer;
import util.JpaUtil;

import java.util.List;

public class CustomerDao implements IDao<Customer, Long> {

    @Override
    public void save(Customer entity) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public Customer findById(Long l) {
        EntityManager em = JpaUtil.getEntityManager();
        Customer customer = null;
        try {
            customer = em.find(Customer.class, l);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return customer;
    }

    @Override
    public List<Customer> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Customer> customers = List.of();
        try {
            TypedQuery<Customer> query =
                    em.createQuery("SELECT c FROM Customer c", Customer.class);
            customers = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return customers;
    }



    @Override
    public void update(Customer entity) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long l) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Customer customer = em.find(Customer.class, l);
            if (customer != null) em.remove(customer);
        tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Customer entity) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Customer.class, id) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public Customer findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Customer> query = em.createQuery(
                    "SELECT c FROM Customer c WHERE c.email = :email", Customer.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("no result");
            return null;
        } finally {
            em.close();
        }
    }

    public Customer findByPublicId(String publicId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Customer> query = em.createQuery("SELECT u FROM Customer u WHERE u.publicId = :publicId", Customer.class);
            query.setParameter("publicId", publicId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("no result");
            return null;
        }finally {
            em.close();
        }
    }
    public Customer findByPhone(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        try{
            TypedQuery<Customer> query = em.createQuery("SELECT c FROM Customer c WHERE c.phoneNumber = :phone", Customer.class);
            query.setParameter("phone", phone);
            return query.getSingleResult();
        }  catch (NoResultException e) {
            System.out.println("no result");
            return null;
        }finally {
            em.close();
        }
    }


}
