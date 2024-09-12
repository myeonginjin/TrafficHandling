package com.couponevent.coupon2.domain;


import com.couponevent.coupon2.constant.Event;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Gifticon {    //발행된 당첨 쿠폰 즉, 기프티콘
    private Event event;
    private String code;

    public Gifticon(Event event) {
        this.event = event;
        this.code = UUID.randomUUID().toString();  //기프티콘 별 고유 번호 부여
    }
}
