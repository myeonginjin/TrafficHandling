package com.couponevent.coupon2.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Event {
    CHICKEN("치킨"),
    PIZZA("피자"),
    COFFEE("커피");

    private String name;
}
