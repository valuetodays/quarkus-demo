package cn.valuetodays.api2.module.fortune.service.module;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-05 11:52
 */
@Data
public class InvestStrategyResp implements Serializable {
    private InvestStrategyReq req;
    // 交易次数
    private int times;
    // 总计投入金额
    private double totalUsedMoney = 0;
    // 总计持有股数
    private int totalShares = 0;
    // 期末市值 / 当前市值
    private double currentMarketValue;
    // 总盈利
    private double totalBenefit;
    // 总红分金额
    private double totalFenhongMoney;
    // 详情
    private List<Detail> details;

    @Data
    public static class Detail implements Serializable {
        private int seqNum;
        private int minTime;
        private Buy buy;
        private Sell sell;

        @Data
        public static class Buy implements Serializable {
            // 交易单价
            private double price;
            private int shares;
            // 交易金额
            private double tradeAmount;
            // 说明
            private String note;
        }

        @Data
        public static class Sell implements Serializable {
            // 交易单价
            private double price;
            private int shares;
            // 交易金额
            private double tradeAmount;
            // 说明
            private String note;
        }
    }

}
