package cn.valuetodays.api2.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.service.module.FixedDayAndMoneyInvestStrategy;
import cn.valuetodays.api2.module.fortune.service.module.InvestStrategy;
import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyReq;
import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyResp;
import cn.valuetodays.api2.module.fortune.service.module.LatestYearsLowPositionStrategy;
import cn.valuetodays.api2.module.fortune.service.module.PbPercentageInvestStrategy;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * @author lei.liu
 * @since 2023-05-05 11:48
 */
@Path("/investStrategy")
public class InvestStrategyFeignController {

    @Inject
    FixedDayAndMoneyInvestStrategy fixedDayAndMoneyInvestStrategy;
    @Inject
    PbPercentageInvestStrategy pbPercentageInvestStrategy;
    @Inject
    LatestYearsLowPositionStrategy latestYearsLowPositionStrategy;

    @Path("/simulate")
    @POST
    public InvestStrategyResp simulate(InvestStrategyReq req) {
        InvestStrategy isToUse;
        InvestStrategyReq.Type type = req.getType();
        if (InvestStrategyReq.Type.FIX == type) {
            isToUse = fixedDayAndMoneyInvestStrategy;
        } else if (InvestStrategyReq.Type.PB == type) {
            isToUse = pbPercentageInvestStrategy;
        } else if (InvestStrategyReq.Type.LATEST3YEAR == type) {
            isToUse = latestYearsLowPositionStrategy;
        } else {
            return new InvestStrategyResp();
        }
        return isToUse.doInvest(req);
    }

}
