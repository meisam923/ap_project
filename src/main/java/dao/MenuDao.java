package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.Menu;
import util.JpaUtil;

import java.util.List;
import java.util.function.Consumer;

public class MenuDao implements IDao<Menu, Integer> {

    @Override
    public void save(Menu menu) {
        executeInTransaction(em -> em.persist(menu));
    }

    @Override
    public Menu findById(Integer id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Menu.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public List<Menu> getAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Menu> query = em.createQuery("SELECT m FROM Menu m", Menu.class);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    @Override
    public void update(Menu menu) {
        executeInTransaction(em -> em.merge(menu));
    }

    @Override
    public void deleteById(Integer id) {
        executeInTransaction(em -> {
            Menu menu = em.find(Menu.class, id);
            if (menu != null) {
                em.remove(menu);
            }
        });
    }

    @Override
    public void delete(Menu menu) {
        executeInTransaction(em -> {
            if (!em.contains(menu)) {
                em.remove(em.merge(menu));
            } else {
                em.remove(menu);
            }
        });
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id) != null;
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
            throw new RuntimeException("Database transaction failed", e);
        } finally {
            if (em != null) em.close();
        }
    }
}