package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.StockMinutePriceServiceImpl;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-24
 */
//@Component
@Slf4j
public class StockMinutePriceTask {
    @Inject
    StockMinutePriceServiceImpl stockMinutePriceService;

    @Scheduled(cron = "17 1 15 * * ?") // 每天15:02:17
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        log.info("begin to refresh");
        stockMinutePriceService.refreshByAsync(null);
        log.info("end to refresh");
    }

    @Scheduled(cron = "3 30 11 * * ?") // 每天午盘休息时
//    @DistributeLock(id = "scheduleRefreshAtNoon", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshAtNoon() {
        scheduleRefresh();
    }

    /**
     * 再次调用，以便第一次调用时服务重启等原因而失败了
     */
    @Scheduled(cron = "17 16 23 * * ?") // 每天23:16:17
//    @DistributeLock(id = "scheduleRefreshTwice", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshTwice() {
        scheduleRefresh();
    }
}
