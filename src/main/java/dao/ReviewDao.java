package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Review;
import util.JpaUtil;

public class ReviewDao {
    public Review findById (Long id) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Review.class, id);
        } finally {
            em.close();
        }
    }
    public void update (Review review) throws Exception {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(review);
            tx.commit();
        } finally {
            em.close();
        }
    }
}
