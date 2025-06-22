package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.client.enums.StockAlertEnums;
import cn.valuetodays.api2.module.fortune.service.StockAlertService;
import cn.valuetodays.api2.module.fortune.util.StockUtils;
import cn.valuetodays.api2.web.common.IVtNatsClient;
import cn.valuetodays.quarkus.commons.base.RunAsync;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-04-16
 */
@ApplicationScoped
@Slf4j
public class StockAlertTask extends RunAsync {
    @Inject
    StockAlertService stockAlertService;
    @Inject
    IVtNatsClient vtNatsClient;
    /**
     * 要比 cn.valuetodays.module.fortune.task.QuoteDailyStatTask#scheduleRefreshAll() 晚
     */
    @Scheduled(cron = "10 57 14 ? * MON-FRI") // 每天14:57:10
//    @DistributeLock(id = "scheduleRefreshAfterMarketClose", milliSeconds = TimeConstants.T3m)
    public void scheduleRefreshAfterMarketClose() {
        if (StockUtils.isInTradeTime()) {
            super.executeAsync(() -> {
                vtNatsClient.publishApplicationMessage(StockAlertTask.class.getSimpleName() + "#scheduleRefreshAfterMarketClose() begin");
                log.info("begin to refresh scheduleRefreshAfterMarketClose");
                stockAlertService.scheduleAlert(StockAlertEnums.ScheduleType.CLOSE);
                log.info("end to refresh scheduleRefreshAfterMarketClose");
                vtNatsClient.publishApplicationMessage(StockAlertTask.class.getSimpleName() + "#scheduleRefreshAfterMarketClose() end");
            });
        }
    }

    @Scheduled(cron = "0 0/10 * ? * MON-FRI") // 每10分钟
//    @DistributeLock(id = "scheduleRefresh10Min", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh10Min() {
        if (StockUtils.isInTradeTime()) {
            super.executeAsync(() -> {
                log.info("begin to refresh scheduleRefresh10Min");
                stockAlertService.scheduleAlert(StockAlertEnums.ScheduleType.EVERY_10_MIN);
                log.info("end to refresh scheduleRefresh10Min");
            });
        }
    }

    @Scheduled(cron = "0 0/20 * ? * MON-FRI") // 每20分钟
//    @DistributeLock(id = "scheduleRefresh20Min", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh20Min() {
        if (StockUtils.isInTradeTime()) {
            super.executeAsync(() -> {
                log.info("begin to refresh scheduleRefresh20Min");
                stockAlertService.scheduleAlert(StockAlertEnums.ScheduleType.EVERY_20_MIN);
                log.info("end to refresh scheduleRefresh20Min");
            });
        }
    }
}
