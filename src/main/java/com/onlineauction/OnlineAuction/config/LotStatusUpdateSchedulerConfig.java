package com.onlineauction.OnlineAuction.config;

import com.onlineauction.OnlineAuction.service.LotService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class LotStatusUpdateSchedulerConfig {

    private final LotService lotService;

    public LotStatusUpdateSchedulerConfig(LotService lotService) {
        this.lotService = lotService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLotStatuses() {
        lotService.updateLotStatusesDateClosing();
    }
}

