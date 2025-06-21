package cn.valuetodays.api2.web.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyReq;
import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyResp;
import cn.valuetodays.api2.module.fortune.service.module.LatestYearsLowPositionStrategy;
import cn.vt.util.DateUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-22 08:24
 */
@Slf4j
class LatestYearsLowPositionStrategyIT implements InvestStrategyTestBase {
    @Inject
    LatestYearsLowPositionStrategy strategy;

    @Test
    void doInvest() {
        final String code = "000001";
        InvestStrategyReq req = new InvestStrategyReq();
        req.setTitle("近2年低位购买");
        req.setDescription("");
        req.setStockCode(code);
        req.setBeginDate(DateUtils.getDate("2021-01-01").toLocalDate());
        req.setEndDate(DateUtils.getDate("2023-04-30").toLocalDate());
        InvestStrategyResp resp = strategy.doInvest(req);
        String s = formatInvestResult(resp);
        log.info("{}", s);
    }

}
