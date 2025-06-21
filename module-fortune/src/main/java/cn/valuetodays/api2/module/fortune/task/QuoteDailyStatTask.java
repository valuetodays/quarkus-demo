package cn.valuetodays.api2.module.fortune.task;

import java.time.LocalDate;

import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockTradeDayService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-02 13:19
 */
@ApplicationScoped
@Slf4j
public class QuoteDailyStatTask {

    @Inject
    QuoteDailyStatServiceImpl quoteDailyStatService;
    @Inject
    StockTradeDayService stockTradeDayService;

    @Scheduled(cron = "13 0 15 * * ?") // 每天15:00:13
//    @DistributeLock(id = "scheduleRefreshAll", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshAll() {
        if (stockTradeDayService.isOpenDay(LocalDate.now())) {
            log.info("begin to refresh scheduleRefreshAll");
            quoteDailyStatService.refreshAll();
            log.info("end to refresh scheduleRefreshAll");
        }
    }

    @Scheduled(cron = "7 30 11 * * ?") // 每天午盘休息时
//    @DistributeLock(id = "scheduleRefreshAllAtNoon", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshAllAtNoon() {
        scheduleRefreshAll();
    }
}
