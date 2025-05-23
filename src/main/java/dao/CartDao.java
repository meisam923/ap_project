package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Cart;
import util.JpaUtil;

import java.util.List;

public class CartDao implements IDao<Cart, Long> {

    EntityManager em = JpaUtil.getEntityManager();
    EntityTransaction tx = em.getTransaction();
    
    @Override
    public void save(Cart entity) {
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
    public Cart findById(Long l) {
        Cart cart  = null;
        try {
            cart = em.find(Cart.class, l);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return cart;
    }

    @Override
    public List<Cart> getAll() {
        List<Cart> Carts = List.of();
        try {
            TypedQuery<Cart> query =
                    em.createQuery("SELECT c FROM Cart c", Cart.class);
            Carts = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return Carts;
    }

    @Override
    public void update(Cart entity) {
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
        try {
            tx.begin();
            Cart Cart = em.find(Cart.class, l);
            if (Cart != null) em.remove(Cart);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Cart entity) {
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
        try {
            return em.find(Cart.class, id) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
