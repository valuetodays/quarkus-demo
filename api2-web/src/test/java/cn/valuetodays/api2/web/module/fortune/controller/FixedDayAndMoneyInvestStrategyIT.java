package cn.valuetodays.api2.web.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.service.module.FixedDayAndMoneyInvestStrategy;
import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyReq;
import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyResp;
import cn.vt.util.DateUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-05 11:51
 */
@Slf4j
class FixedDayAndMoneyInvestStrategyIT implements InvestStrategyTestBase {

    @Inject
    private FixedDayAndMoneyInvestStrategy fixedDayAndMoneyInvestStrategy;

    @Test
    public void test() {
        final String code = "000001";
        InvestStrategyReq req = new InvestStrategyReq();
        req.setTitle("定期定额定投");
        req.setDescription("");
        req.setStockCode(code);
        req.setBeginDate(DateUtils.getDate("2002-01-01").toLocalDate());
        req.setEndDate(DateUtils.getDate("2016-12-31").toLocalDate());
        InvestStrategyResp resp = fixedDayAndMoneyInvestStrategy.doInvest(req);
        String s = formatInvestResult(resp);
        log.info("{}", s);
    }

}
