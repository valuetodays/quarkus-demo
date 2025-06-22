package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-01
 */
@Data
public class AnalyzeHedgedWithFeeResp implements Serializable {
    private AnalyzeHedgedTradeResp analyzeHedgedTrade;
    private BigDecimal netEarnedCash;
    private BigDecimal interestValue;
    private BigDecimal netEarnedCashPerTradeDay;
    private BigDecimal netEarnedCashPerTradeDayWithWeekends;
}
