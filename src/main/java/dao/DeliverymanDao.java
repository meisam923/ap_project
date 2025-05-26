package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.Customer;
import model.Deliveryman;
import util.JpaUtil;

import java.util.List;

public class DeliverymanDao implements IDao<Deliveryman, Long>{

    @Override
    public void save(Deliveryman entity) {
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
    public Deliveryman findById(Long l) {
        EntityManager em = JpaUtil.getEntityManager();
        Deliveryman dm = null;
        try {
            dm = em.find(Deliveryman.class, l);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return dm;
    }

    @Override
    public List<Deliveryman> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Deliveryman> dms = List.of();
        try {
            TypedQuery<Deliveryman> query =
                    em.createQuery("SELECT dm FROM Deliveryman dm", Deliveryman.class);
            dms = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return dms;
    }

    @Override
    public void update(Deliveryman entity) {
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
            Deliveryman dm = em.find(Deliveryman.class, l);
            if (dm != null) em.remove(dm);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


    @Override
    public void delete(Deliveryman entity) {
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
            return em.find(Deliveryman.class, id) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public Deliveryman findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Deliveryman> query = em.createQuery(
                    "SELECT c FROM Deliveryman c WHERE c.email = :email", Deliveryman.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("no result");
            return null;
        } finally {
            em.close();
        }
    }

    public Deliveryman findByPublicId(String publicId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Deliveryman> query = em.createQuery("SELECT u FROM Deliveryman u WHERE u.publicId = :publicId", Deliveryman.class);
            query.setParameter("publicId", publicId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("no result");
            return null;
        }finally {
            em.close();
        }
    }
    public Deliveryman findByPhone(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        try{
            TypedQuery<Deliveryman> query = em.createQuery("SELECT c FROM Deliveryman c WHERE c.phoneNumber = :phone", Deliveryman.class);
            query.setParameter("phone", phone);
            return query.getSingleResult();
        } catch (NoResultException e) {
            System.out.println("no result");
            return null;
        }finally {
            em.close();
        }
    }
}
