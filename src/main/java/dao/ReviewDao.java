package dao;

import jakarta.persistence.EntityManager;
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
        try {
            em.merge(review);
        } finally {
            em.close();
        }
    }
}
