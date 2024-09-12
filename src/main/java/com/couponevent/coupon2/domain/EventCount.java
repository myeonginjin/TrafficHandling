package com.couponevent.coupon2.domain;

import com.couponevent.coupon2.constant.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventCount {   //이벤트 별로 남은 쿠폰 수를 관리하는 역할을 하는 클래스
    private Event event;
    private int limit;   //해당 이벤트의 발행될 수 있는 최대 쿠폰 수

    private static final int END = 0;

    public EventCount(Event event, int limit) {
        this.event = event;  //어떤 이벤트인지
        this.limit = limit;  //발행할 쿠폰은 몇개인지
    }
            //스레드에서 동시에 접근할 경우에도 안전하게 처리하도록(동시성 이슈가 발생하지 않도록) synchronized
    public synchronized void decrease(){
        this.limit--;  //발행할 수 있는 현재 쿠폰 수를 줄이는 메서드
    }

    //이벤트가 끝났는지 확인하는 메서드
    public boolean end(){
        return this.limit == END;
    }
}
