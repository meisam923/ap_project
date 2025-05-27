package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Menu;
import util.JpaUtil;

public class MenuDao {
    public void save(Menu menu) throws Exception {
        EntityManager em= JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            em.persist(menu);
            tx.commit();
        } finally {
            em.close();
        }
    }

    public void delete(Menu menu) throws Exception {
        EntityManager em= JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            em.remove(menu);
            tx.commit();
        }
        finally {
            em.close();
        }
    }

    public void update(Menu menu) throws Exception {
        EntityManager em= JpaUtil.getEntityManager();
        EntityTransaction tx=em.getTransaction();
        try {
            tx.begin();
            em.merge(menu);
            tx.commit();
        }
        finally {
            em.close();
        }
    }
}
