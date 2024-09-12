package com.couponevent.coupon2.domain;


import com.couponevent.coupon2.constant.Event;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Gifticon {
    private Event event;
    private String code;

    public Gifticon(Event event) {
        this.event = event;
        this.code = UUID.randomUUID().toString();
    }
}
