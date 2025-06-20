package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.EtfT0Service;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-01 18:59
 */
@ApplicationScoped
@Slf4j
public class EtfT0Task {

    @Inject
    EtfT0Service etfT0Service;

    @Scheduled(cron = "1 1 15 * * ?") // 每天15:01:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        log.info("begin to refresh etfT0");
        etfT0Service.refresh();
        log.info("end to refresh etfT0");
    }

    @Scheduled(cron = "1 15 22 * * ?") // 每天22:15:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshTwice() {
        scheduleRefresh();
    }

}
