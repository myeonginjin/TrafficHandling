package com.couponevent.coupon1;


import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class CouponTest {

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponRepository couponRepository;

    @Test
    @Transactional
    void 한번만응모() {
        couponService.apply(1L);
        long count = couponRepository.count();

        System.out.println("count = " + count);
        assertEquals(count, 1L);
    }

    @Test
    @Transactional
    void 여러번응모() throws InterruptedException {
        int threadCount = 1000;

        // 32개의 스레드풀 생성
        // 만약 여기를 1로 수정하고 JPA레포지토리를 쓰면 레디스처럼 동시성 문제를 해결해준다 (단 카운트와 인크리먼트 차이가 있으므로 count 초과를 이상으로 바꿔줘야함)
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 1000개의 작업이 끝나야 다음으로 진행할 수 있도록 하는 장치의 클래스
        CountDownLatch latch = new CountDownLatch(threadCount);

    /*
        1. 반복문 내의 executorService.submit()는 비동기적으로 로직을 수행한다.
        2. 그래서 메인스레드는 반복문에서 내려와 로직을 끝내게 되는데,
        3. latch.await()이 있으므로 메인스레드는 이 모든 latch들의 작업이 끝날 때 까지 대기상태에 빠진다.
        4. 이후 count가 0에 다다르면 메인 스레드를 대기상태로부터 깨워 남은 로직을 수행하게 한다.
     */


        //존나 신기한점 2 여기 반복문 범위가 1000보다 적으면 무한대기 됨. 메인스레드는 백스레드의 1000개의 작업 끝나길 기다리고
        // 백스레드는 일 받기를 기다림
        for (int i = 1; i <= 1000; i++) {
            long memberId = i;
            executorService.submit(() -> {
                try {
                    couponService.apply(memberId);
                } finally {

                    // 1000부터 줄여나가서 0이 되면 메인 스레드를 대기상태에서 해제한다.
                    latch.countDown();

                }
            });
        }

        // 메인 스레드를 대기상태로 전환한다.
        latch.await();   // ㅈㄴ신기한점 ! 만약 반복문의 범위 끝값의 숫자가 겁나게 크면 기더려주지않아도 된다.
        //Thread.sleep(5000); 이렇게 해도 됨. 왜? 백스레드들이 비동기 일을 끝낼떄까지 기다리니까.

        long count = couponRepository.count();
        System.out.println("총 발급된 쿠폰의 수 count = " + count);
        assertThat(count).isEqualTo(100);
    }

}

        /*
        메인스레드는 i를 1씩 증가시키면서 1000까지 도달한다.
        이때, 동기가 아니기 때문에 i가 1,2...일때마다 몸체 내부를 수행하는게 아니라 메인메서드는 i에 1부터 1000씩의 숫자를 집어 넣고,
        executorService.submit(()을 통해 백스레드(32개)에게 일을 던지고 반복문을 이어간다. 즉, memberId에 각 숫자가 담길 때마다
        32개 스레드가 이 일을 처리하고 다음 숫자로 넘어가는 것이 아니라 memberId가 1일때 , 2일때 ...1000일때 각 상황아마다 32개의 스레드가 처리해야할 일이

        <"각 작업이 32개의 스레드 풀에 비동기적으로 분배된다가 더 적절합니다. 스레드 풀이 32개이기 때문에 동시에 최대 32개의 작업이 실행되며, 나머지는 큐에 대기하게 됩니다.">

        쌓여있는 것이다. 메인스레드는 1000까지 멤버 아이디에 숫자를 넣으면 이제 그만 반복문을 탈출해 버리므로 각 memberID (1~1000)상황에 일이
        처리될 때까지 아래에서 대기하도록한다. 즉, 1000개의 처리해야할 일이 있는 것이고 각 일은 32개의 일꾼이 각 일을 동시에 해결하려한다. 레디스는 싱글스레드로 동작하기 때문에
        일꾼이 32명이더라도 한명만 일하는 것처럼 일해, 동시성 이슈가 발생하지않는다.

        <Redis는 싱글스레드로 동작하는 게 맞지만, 그럼에도 동시성 이슈가 발생하지 않는 이유는 Redis의 원자적 연산 덕분입니다. Redis가 싱글스레드라서 동시성 문제가 해결되는 것이 아니라, INCR 같은 원자적 연산 덕분에 여러 스레드가 동시에 값을 수정하려 할 때도 정확하게 처리됩니다.
	•	Redis가 싱글스레드라서 동시에 많은 요청을 처리하지 못할 것처럼 보일 수 있지만, Redis는 높은 성능을 제공하는 데 최적화되어 있어 빠르게 요청을 처리합니다.>


        단, 멀티스레드 환경인 mysqld은 32개의 일꾼이 이제 발급받을 수 있는 쿠폰이 한장 남은 상황에서도
        각자 하나씩 더 발급해주려고 하다보니 100개가 넘게 쿠폰이 발생하는 일이 생긴다. 만약 일할 스레드를 하나로 줄인다면 mysql을 사용한다해도 동시성 이슈가 발생하지않는다.
        */


// GPT 주석 수정글
        /*
            메인스레드는 i를 1씩 증가시키면서 1000까지 도달한다.
            이때, 비동기적으로 동작하기 때문에 i가 1일 때, 2일 때마다 바로 각 스레드에게 작업을 할당한다.
            executorService.submit()을 통해 32개의 스레드가 동시에 여러 작업을 처리하게 되고, 각 작업은 큐에 쌓여 차례로 처리된다.

            Redis는 싱글스레드로 동작하며, **원자적 연산(Atomic Operations)**을 제공하기 때문에 여러 스레드가 동시에 쿠폰 발급을 시도하더라도
            동시성 문제가 발생하지 않는다. 여러 스레드가 동시에 Redis에 값을 쓰려고 할 때, Redis는 원자적으로 값을 처리하여
            정확하게 100명에게만 쿠폰이 발급된다.

            반면, 멀티스레드 환경에서 MySQL을 사용하면 경합 조건(Race Condition)이 발생할 수 있어 쿠폰이 초과 발급될 가능성이 있다.
            이를 방지하기 위해 Redis를 사용하는 것이 적절하다.

            latch.await()는 모든 스레드가 작업을 완료할 때까지 메인 스레드를 대기시켜 백그라운드 작업이 끝날 때까지 기다리도록 한다.
            만약 latch.await()을 사용하지 않으면 메인 스레드가 너무 빨리 종료되어 백그라운드 작업이 완료되지 않을 수 있다.
        */
