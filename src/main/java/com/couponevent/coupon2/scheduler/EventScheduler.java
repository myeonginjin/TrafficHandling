package com.couponevent.coupon2.scheduler;
import com.couponevent.coupon2.constant.Event;
import com.couponevent.coupon2.service.GifticonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final GifticonService gifticonService;

    // 얘는 그냥 어플리케이션이 돌아가는 시점부터 1초마다 계속 실행되는 거임 근데 sorted set비어져 있으니까 뭐 발급될거랑 조회될게 없던거
    @Scheduled(fixedDelay = 1000)      //스케줄 이용해서 주기적으로 작업 처리하도록
    private void chickenEventScheduler(){
        if(gifticonService.validEnd()){
            log.info("===== 선착순 이벤트가 종료되었습니다. =====");
            return;
        }
        gifticonService.publish(Event.CHICKEN);    //기프티콘 발급
        gifticonService.getOrder(Event.CHICKEN);   //현재 대기열 조회
    }
}
