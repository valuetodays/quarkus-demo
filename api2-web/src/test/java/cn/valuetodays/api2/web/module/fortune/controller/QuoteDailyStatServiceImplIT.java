package cn.valuetodays.api2.web.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.valuetodays.api2.module.fortune.reqresp.AShareHistoryTurnAmountResp;
import cn.valuetodays.api2.module.fortune.reqresp.AShareLatestTurnAmountResp;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatReq;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteStatGroupResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteStatResp;
import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import cn.vt.rest.third.eastmoney.QuoteEnums;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-02 13:40
 */
@Slf4j
class QuoteDailyStatServiceImplIT {

    @Inject
    private QuoteDailyStatServiceImpl indexDailyStatService;

    @Test
    void overview() {
        AShareLatestTurnAmountResp overviews = indexDailyStatService.aShareLatestTurnAmount();
        log.info("overviews: {}", overviews);
    }

    @Test
    void refreshAll() {
        indexDailyStatService.refreshAll();
    }

    @EnabledOnOs(OS.WINDOWS)
    @Test
    void refreshOneFullyWithCodes() {
        QuotePO sh000001 = new QuotePO();
        sh000001.setCode("000001");
        sh000001.setRegion(QuoteEnums.Region.SHANGHAI);
        QuotePO sz399001 = new QuotePO();
        sz399001.setCode("399001");
        sz399001.setRegion(QuoteEnums.Region.SHENZHEN);
        List.of(sh000001, sz399001).forEach(e -> indexDailyStatService.refreshOneFully(e));
    }

    @Test
    void aShareHistoryTurnAmount() {
        AShareHistoryTurnAmountResp resp = indexDailyStatService.aShareHistoryTurnAmount();
        log.info("resp: {}", resp);
    }

    @Test
    public void determineHighestAndLowestFor000300() {
        List<QuoteDailyStatPO> list;
        // 测试沪深300
        String code = "000300";
        list = indexDailyStatService.findAllByCodeOrderByMinTimeDesc(code);
        log.info("list.size: {}", list.size());
        log.info("请去执行IndexDailyStatServiceImplMainTest.determineHighestAndLowestFor000300()");
    }


    @Test
    public void computeStats() {
        // 测试沪深300
        String code = "000300";
        QuoteStatGroupResp quoteStatGroupResp = indexDailyStatService.computeStats(code);
        List<QuoteStatResp> list = quoteStatGroupResp.getIndexStatList();
        for (QuoteStatResp isr : list) {
            log.info("isr: {}", isr);
        }

    }

    @Test
    public void computeDailyOffsetStat() {
        // 测试沪深300
        String code = "000300";
        DailyOffsetStatReq req = new DailyOffsetStatReq();
        req.setCode(code);
        DailyOffsetStatResp resp = indexDailyStatService.computeDailyOffsetStat(req);
        log.info("resp: {}", resp);
    }

    @Test
    public void computeListForOverview() {
        String code = "000300";
        List<QuoteDailyStatPO> list = indexDailyStatService.computeMonthlySuggest(code);


    }


}
