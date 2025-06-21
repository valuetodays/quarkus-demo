package cn.valuetodays.api2.module.fortune.service.module;

import java.util.ArrayList;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * 每月定期定投.
 *
 * @author lei.liu
 * @since 2023-05-05 10:44
 */
@Slf4j
@ApplicationScoped
public class FixedDayAndMoneyInvestStrategy implements InvestStrategy {
    @Inject
    private QuoteDailyStatServiceImpl quoteDailyStatService;

    @Override
    public InvestStrategyResp doInvest(InvestStrategyReq req) {
        String code = req.getStockCode();
        List<QuoteDailyStatPO> dataList = quoteDailyStatService.findAllByCodeOrderByMinTimeDesc(code);
        List<QuoteDailyStatPO> tradeDays = InvestStrategy.computeTradeDays(req, dataList);

        // 每月预期投入金额
        double expectedUseMoneyPerMonth = 2000;
        // 总计投入金额
        double totalUsedMoney = 0;
        // 总计持有股数
        int totalShares = 0;
        //
        double fenhongMoney = 0;
        int fenhongShares = 0;

        List<InvestStrategyResp.Detail> details = new ArrayList<>(tradeDays.size());

        for (int i = 0; i < tradeDays.size(); i++) {
            QuoteDailyStatPO tradeDay = tradeDays.get(i);

            double closePx = tradeDay.getClosePx();
            // 价格 = 收盘点数/1000
            double price = closePx / 1000;
            int shares = (int) (expectedUseMoneyPerMonth / price);
            totalShares += shares;

            int moneyUsedInThisMonth = (int) Math.floor(price * shares);
            totalUsedMoney += moneyUsedInThisMonth;
            int statDateInTradeDay = tradeDay.getStatDate();

            InvestStrategyResp.Detail detail = new InvestStrategyResp.Detail();
            detail.setSeqNum(i + 1);
            detail.setMinTime(statDateInTradeDay);
            InvestStrategyResp.Detail.Buy buy = new InvestStrategyResp.Detail.Buy();
            buy.setPrice(price);
            buy.setShares(shares);
            buy.setTradeAmount(moneyUsedInThisMonth);
            detail.setBuy(buy);
            details.add(detail);
        }

        double currentMarketValue = tradeDays.get(tradeDays.size() - 1).getClosePx() / 1000 * totalShares;

        InvestStrategyResp resp = new InvestStrategyResp();
        resp.setReq(req);
        resp.setTimes(tradeDays.size());
        resp.setTotalUsedMoney(totalUsedMoney);
        resp.setTotalShares(totalShares);
        resp.setCurrentMarketValue(currentMarketValue);
        resp.setTotalBenefit((currentMarketValue - totalUsedMoney));
        resp.setDetails(details);
        return resp;
    } // end of doInvest()


}
