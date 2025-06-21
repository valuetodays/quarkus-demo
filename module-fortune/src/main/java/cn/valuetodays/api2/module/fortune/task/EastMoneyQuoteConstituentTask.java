package cn.valuetodays.api2.module.fortune.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

import cn.valuetodays.api2.module.fortune.service.QuoteConstituentServiceImpl;
import cn.valuetodays.api2.module.fortune.service.module.EastMoneyQuoteConstituentModule;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class EastMoneyQuoteConstituentTask {

    @Inject
    EastMoneyQuoteConstituentModule module;
    @Inject
    QuoteConstituentServiceImpl quoteConstituentService;

    @Scheduled(cron = "1 7 15 ? * THU") // 每周四15:07:01
//    @DistributeLock(id = "scheduleQuoteConstituent", milliSeconds = TimeConstants.T3m)
    public void scheduleQuoteConstituent() {
        log.info("begin to scheduleQuoteConstituent");
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(dayOfWeek)) {
            log.info("today is {}. stock markets were closed", dayOfWeek.name());
            return;
        }
        module.scheduleGather();
        log.info("end to scheduleQuoteConstituent");
    }

    @Scheduled(cron = "16 16 2 * * ?") // 每天02:16:16
//    @DistributeLock(id = "scheduleSaveDividends", milliSeconds = TimeConstants.T3m)
    public void scheduleSaveDividends() {
        log.info("begin to scheduleSaveDividends");
        quoteConstituentService.scheduleSaveDividends();
        log.info("end to scheduleSaveDividends");
    }

//    @PostConstruct
//    public void postConstruct() {
//        quoteConstituentService.scheduleSaveDividends();
//    }

}
