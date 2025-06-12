package Controller;

import dao.CouponDao;
import dto.CouponDto;
import jakarta.persistence.EntityManager;
import model.Coupon;
import util.JpaUtil;

import java.util.Optional;

public class CouponController {
    CouponDao couponDao = new CouponDao();

    public boolean doesCouponExist(Integer id) {
        return couponDao.findById(id) != null;
    }
    public boolean doesCouponExist(String code) {
        return couponDao.findByCode(code).isPresent();
    }

    public Optional<CouponDto.CouponSchemaDTO> getValidCouponByCode(String couponCode) {
        Optional<Coupon> couponOpt = couponDao.findByCode(couponCode);

        if (couponOpt.isEmpty()) {
            return Optional.empty();
        }

        Coupon coupon = couponOpt.get();

        CouponDto.CouponSchemaDTO couponDto = new CouponDto.CouponSchemaDTO(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getType().name(),
                coupon.getValue(),
                coupon.getMinPrice(),
                coupon.getUserCount(),
                coupon.getStartDate(),
                coupon.getEndDate()
        );

        return Optional.of(couponDto);
    }
}
