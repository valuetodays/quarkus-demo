package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.EtfIopvService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-09-16
 */
@ApplicationScoped
public class EtfIopvTask {

    @Inject
    EtfIopvService etfIopvService;

    @Scheduled(cron = "1 2 15 * * ?") // 每天15:02:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        etfIopvService.gatherAndSave(false);
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
