package dao;

import jakarta.persistence.EntityManager;
import model.Admin;
import model.Deliveryman;
import util.JpaUtil;

public class AdminDao {
    public Admin findById(Long l) {
        EntityManager em = JpaUtil.getEntityManager();
        Admin ad = null;
        try {
            ad = em.find(Admin.class, l);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            em.close();
        }
        return ad;
    }

}
