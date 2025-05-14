package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Owner;
import util.JpaUtil;

import java.util.List;

public class OwnerDao implements IDao<Owner, Long>{
    @Override
    public void save(Owner entity) {
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
    public Owner findById(Long l) {
        EntityManager em = JpaUtil.getEntityManager();
        Owner o = null;
        try {
            o = em.find(Owner.class, l);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return o;
    }

    @Override
    public List<Owner> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Owner> os = List.of();
        try {
            TypedQuery<Owner> query =
                    em.createQuery("SELECT os FROM Owner os", Owner.class);
            os = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return os;
    }

    @Override
    public void update(Owner entity) {
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
            Owner o = em.find(Owner.class, l);
            if (o != null) em.remove(o);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Owner entity) {
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
            return em.find(Owner.class, id) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
