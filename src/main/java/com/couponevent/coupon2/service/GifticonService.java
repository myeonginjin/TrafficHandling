package com.couponevent.coupon2.service;


import com.couponevent.coupon2.constant.Event;
import com.couponevent.coupon2.domain.EventCount;
import com.couponevent.coupon2.domain.Gifticon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GifticonService {     //기프티콘 발급과 대기열 관리
    private final RedisTemplate<String,Object> redisTemplate;
    private static final long FIRST_ELEMENT = 0;
    private static final long LAST_ELEMENT = -1;
    private static final long PUBLISH_SIZE = 10;
    private static final long LAST_INDEX = 1;
    private EventCount eventCount;
    static int cnt = 1;

    // 이벤트의 남은 쿠폰 수를 설정
    public void setEventCount(Event event, int queue){
        this.eventCount = new EventCount(event, queue);
    }

    //대기열에 사람 추가
    public void addQueue(Event event){
        final String people = Integer.toString(cnt++);
        final long now = System.currentTimeMillis();
        //log.info("대기열에 추가 - {} ({}초)", people, now);   //엥? 유효성은 맞는데 왜 로그는 1번 요청부터 안 찍히지?
        //왜 등록은 1번부터 잘 되더라도 로그찍히는 번호는 1번부터가 아니지? 그래도 레디스에는 요청 순서대로 잘 들어가지긴 함
        System.out.println("대기열에 추가 - {} ({}초)"+" "+ people +" "+ now);

        //현재 스레드 이름과 시간을 기준으로 sorted set에 추가 (시간순으로 자동 정렬)
        redisTemplate.opsForZSet().add(event.toString(), people, (int) now);
        //이때, 추가되는 작업과 스케줄러의 상위 10명씩 당첨시켜버리는 작업은 병렬적으로, 독립적으로 진행되고 있음
    }

    public void getOrder(Event event){
        final long start = FIRST_ELEMENT;
        final long end = LAST_ELEMENT;

        //대기열에 있는 사람들의 순서를 가져와 로그 찍기
        Set<Object> queue = redisTemplate.opsForZSet().range(event.toString(), start, end);

        for (Object people : queue) {
            Long rank = redisTemplate.opsForZSet().rank(event.toString(), people);
            log.info("'{}'님의 현재 대기열은 {}명 남았습니다.", people, rank);
        }
    }

    // 대기열의 상위 사람들에게 기프티콘을 발급하는 메서드, GiftiCon 객체 생성 및 발급 후 대기열에 해당 유저 제거
    public void publish(Event event){
        final long start = FIRST_ELEMENT;
        final long end = PUBLISH_SIZE - LAST_INDEX;

        Set<Object> queue = redisTemplate.opsForZSet().range(event.toString(), start, end);
        for (Object people : queue) {
            final Gifticon gifticon = new Gifticon(event);
            log.info("'{}'님의 {} 기프티콘이 발급되었습니다 ({})",people, gifticon.getEvent().getName(), gifticon.getCode());
            redisTemplate.opsForZSet().remove(event.toString(), people);
            this.eventCount.decrease();
        }
    }

    //이벤트 종료 여부 확인
    public boolean validEnd(){
        return this.eventCount != null
                ? this.eventCount.end()
                : false;
    }

    //현재 대기열 크기 반환
    public long getSize(Event event){
        return redisTemplate.opsForZSet().size(event.toString());
    }

}
