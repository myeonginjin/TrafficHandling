package com.couponevent.coupon2;

import com.couponevent.coupon2.constant.Event;
import com.couponevent.coupon2.service.GifticonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ScheduledTest {

    @Autowired
    private GifticonService gifticonService;


    @Test
    void 선착순이벤트_100명에게_기프티콘_30개_제공() throws InterruptedException {
        final Event chickenEvent = Event.CHICKEN;
        final int people = 100;
        final int limitCount = 30;
        final CountDownLatch countDownLatch = new CountDownLatch(people);
        gifticonService.setEventCount(chickenEvent, limitCount);    //이벤트 종류는 치킨이고, 30명 쿠폰 발급이 가능한 이벤트로 이벤트 관리 클래스인 EventCount 인스턴스 생성

        List<Thread> workers = Stream
                .generate(() -> new Thread(new AddQueueWorker(countDownLatch, chickenEvent)))  //.generate는 반복적으로 특정 값을 생성하는 함수, 여기서는 참가자를 표현하는 스레드를 반복적으로 생성
                .limit(people) //100개만 생성하도록 제한
                .collect(Collectors.toList());  //이렇게 생성된 스레드 객체들을 리스트로 반환하도록 함

        workers.forEach(Thread::start);   //Thread의 start메서드를 실행시키면 Runnable를 구현한 클래스의 run() 메서드가 실행됨. 즉, AddQueueWorker의 run()이 실행됨. 각 스레드가 비동기적으로 run()을 실행
        countDownLatch.await();  //대기열에 100이 모두 등록될 때 까지 메인 스레드가 기다려줌
        Thread.sleep(5000); // 기프티콘 발급 스케줄러 작업 시간 기다려줌 발행 작업 끝날때까지. 대기열 등록만 기다려서는 부족함. 대기열 등록은 끝났어도 30명 배정이 아직 안 끝남

        final long failEventPeople = gifticonService.getSize(chickenEvent);
        assertEquals(people - limitCount, failEventPeople); // output : 70 = 100 - 30
    }

    private class AddQueueWorker implements Runnable {
        private CountDownLatch countDownLatch;
        private Event event;

        public AddQueueWorker(CountDownLatch countDownLatch, Event event) {
            this.countDownLatch = countDownLatch;
            this.event = event;
        }

        @Override
        public void run() {
            gifticonService.addQueue(event);  //sorted set에 아이템 (유저 요청)이 추가되면서 대기열에 발행 대기자들이 생기게되고, 스케줄러에 의해 계속 돌아가던 이벤트 스케줄러 클래스의 chickenEventScheduler가 대기열의 사람들에게 쿠폰 발행
            countDownLatch.countDown();
        }
    }

}
