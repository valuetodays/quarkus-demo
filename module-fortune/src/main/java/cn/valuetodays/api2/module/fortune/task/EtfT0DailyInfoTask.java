package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.EtfT0DailyInfoService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-04
 */
@ApplicationScoped
public class EtfT0DailyInfoTask {
    @Inject
    EtfT0DailyInfoService etfT0DailyInfoService;

    @Scheduled(cron = "1 2 15 * * ?") // 每天15:02:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        etfT0DailyInfoService.refresh();
    }

    @Scheduled(cron = "1 15 19 * * ?") // 每天19:15:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshTwice() {
        scheduleRefresh();
    }

    @Scheduled(cron = "1 2 22 * * ?") // 每天22:02:01
//    @DistributeLock(id = "fixTotalShares", milliSeconds = TimeConstants.T3m)
    public void fixTotalShares() {
        etfT0DailyInfoService.fixTotalShares();
    }

    @Scheduled(cron = "1 2 23 * * ?") // 每天23:02:01
//    @DistributeLock(id = "fixTotalShares", milliSeconds = TimeConstants.T3m)
    public void fixTotalSharesTwice() {
        fixTotalShares();
    }

}
