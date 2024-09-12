package com.couponevent.coupon1;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;


    public void apply(Long memberId) {
       //long count = couponRepository.count(); //동시성 이슈 발생


        Long count = couponCountRepository.increase();

        System.out.println(memberId +" " +count );

        if (count > 100) {
            return;
        }

        couponRepository.save(new Coupon(memberId));
    }
}