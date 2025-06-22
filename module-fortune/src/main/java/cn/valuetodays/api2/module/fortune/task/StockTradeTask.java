package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.StockTradeServiceImpl;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-10-11
 */
@ApplicationScoped
@Slf4j
public class StockTradeTask {
    @Inject
    StockTradeServiceImpl stockTradeService;

    @Scheduled(cron = "1 5 15 * * ?") // 每天15:05:01
//    @DistributeLock(id = "checkHedgedList", milliSeconds = TimeConstants.T3m)
    public void checkHedgedList() {
        stockTradeService.checkHedgedList();
    }

    @Scheduled(cron = "1 5 20 * * ?") // 每天20:05:01
//    @DistributeLock(id = "checkHedgedListTwice", milliSeconds = TimeConstants.T3m)
    public void checkHedgedListTwice() {
        this.checkHedgedList();
    }

}
