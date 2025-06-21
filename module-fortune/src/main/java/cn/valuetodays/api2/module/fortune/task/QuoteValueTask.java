package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.QuoteValueServiceImpl;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class QuoteValueTask {
    @Inject
    QuoteValueServiceImpl quoteValueService;

    @Scheduled(cron = "1 30 4 * * ?") // 每天05:30:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        log.info("begin to refresh quoteValue");
        quoteValueService.refresh();
        log.info("end to refresh quoteValue");
    }

}
