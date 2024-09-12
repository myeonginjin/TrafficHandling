package com.couponevent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CouponEventApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponEventApplication.class, args);
    }

}
