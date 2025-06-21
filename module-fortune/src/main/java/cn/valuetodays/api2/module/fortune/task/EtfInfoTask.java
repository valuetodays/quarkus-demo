package cn.valuetodays.api2.module.fortune.task;

import cn.valuetodays.api2.module.fortune.service.EtfInfoServiceImpl;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-07-10 18:59
 */
@ApplicationScoped
@Slf4j
public class EtfInfoTask {

    @Inject
    EtfInfoServiceImpl etfInfoService;

    @Scheduled(cron = "1 15 23 * * ?") // 每天23:15:01
//    @DistributeLock(id = "scheduleRefresh", milliSeconds = TimeConstants.T3m)
    public void scheduleRefresh() {
        log.info("begin to refresh etfInfo");
        etfInfoService.refresh();
        log.info("end to refresh etfInfo");
    }


    // for local-test
//    @PostConstruct
    public void postConstruct() {
        etfInfoService.refresh();
    }
}
