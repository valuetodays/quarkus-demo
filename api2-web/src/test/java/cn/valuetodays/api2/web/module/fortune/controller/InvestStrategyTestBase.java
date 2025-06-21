package cn.valuetodays.api2.web.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.service.module.InvestStrategyResp;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-22 08:35
 */
public interface InvestStrategyTestBase {

    default String formatInvestResult(InvestStrategyResp resp) {
        double currentMarketValue = resp.getCurrentMarketValue();
        double totalUsedMoney = resp.getTotalUsedMoney();
        double totalFenhongMoney = resp.getTotalFenhongMoney();

        StringBuilder sbDetailList = new StringBuilder();

        List<InvestStrategyResp.Detail> details = resp.getDetails();
        for (InvestStrategyResp.Detail d : details) {
            InvestStrategyResp.Detail.Buy buy = d.getBuy();
            InvestStrategyResp.Detail.Sell sell = d.getSell();

            sbDetailList.append(" #").append(d.getSeqNum())
                .append(" ").append(d.getMinTime()).append(":")
                .append(" buy price: ").append(buy.getPrice())
                .append(" buy share: ").append(buy.getShares())
                .append(" buy tradeAmount: ").append(buy.getTradeAmount())
                .append(" buy note: ").append(StringUtils.trimToEmpty(buy.getNote()))
                .append(" | ")
                .append(" sell price: ").append(sell.getPrice())
                .append(" sell share: ").append(sell.getShares())
                .append(" sell tradeAmount: ").append(sell.getTradeAmount())
                .append(" sell note: ").append(StringUtils.trimToEmpty(sell.getNote()))
                .append("\n");
        }

        String sb = "\n --- 交易统计 --- " + "\n"
            + "交易次数：" + resp.getTimes() + "\n"
            + "日期：" + details.get(0).getMinTime() + "~" + details.get(details.size() - 1).getMinTime() + "\n"
            + "总投入：" + totalUsedMoney + "\n"
            + "总股数：" + resp.getTotalShares() + "\n"
            + "总分红金额：" + 0L + "\n"
            + "总分红股数：" + 0L + "\n"
            + "当前市值：" + currentMarketValue + "\n"
            + "总收入：" + ((currentMarketValue - totalUsedMoney) + totalFenhongMoney) + "\n"
            + "\n --- 交易详情 --- \n"
            + sbDetailList;
        return sb;
    }

}
