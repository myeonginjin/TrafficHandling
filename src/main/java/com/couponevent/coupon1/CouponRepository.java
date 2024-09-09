package com.couponevent.coupon1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface CouponRepository extends JpaRepository<Coupon, Long> {

}
