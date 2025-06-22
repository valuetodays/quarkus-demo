package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.StockHistoryPriceServiceImpl;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.liu
 * @since 2021-03-19 18:32
 */
//@Component
@Slf4j
public class StockHistoryPriceRefreshTask {

    @Inject
    StockHistoryPriceServiceImpl stockHistoryPriceService;

    @Scheduled(cron = "1 39 22 * * ?") // 每天22:39:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        log.info("begin to refresh stock price");
        stockHistoryPriceService.refresh(null);
        log.info("end to refresh stock price");
    }

}
