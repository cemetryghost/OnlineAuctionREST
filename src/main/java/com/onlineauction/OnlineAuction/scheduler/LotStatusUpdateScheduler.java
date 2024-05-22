package com.onlineauction.OnlineAuction.scheduler;

import com.onlineauction.OnlineAuction.service.LotService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class LotStatusUpdateScheduler {

    private final LotService lotService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLotStatuses() {
        lotService.updateLotStatusesDateClosing();
    }
}

