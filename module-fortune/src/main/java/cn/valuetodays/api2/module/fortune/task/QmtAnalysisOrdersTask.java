package cn.valuetodays.api2.module.fortune.task;

import java.time.DayOfWeek;
import java.time.LocalDate;

import cn.valuetodays.api2.module.fortune.controller.QmtController;
import cn.valuetodays.api2.web.basic.push.enums.NotifyEnums;
import cn.valuetodays.api2.web.common.NotifyService;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-22
 */
//@Component
@Slf4j
public class QmtAnalysisOrdersTask {

    @Inject
    QmtController qmtController;
    @Inject
    NotifyService notifyService;

    @Scheduled(cron = "5 0 15 * * ?") // 每天15:00:05
//    @DistributeLock(id = "pushAnalysisOrdersResult", milliSeconds = TimeConstants.T3m)
    public void pushAnalysisOrdersResult() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (DayOfWeek.SATURDAY == dayOfWeek || DayOfWeek.SUNDAY == dayOfWeek) {
            return;
        }
        String s = qmtController.analysisOrdersAsString();

        notifyService.notify(
            "【资本市场】分析委托",
            s,
            NotifyEnums.Group.CAPITAL.getTitle(),
            true
        );
    }
}
