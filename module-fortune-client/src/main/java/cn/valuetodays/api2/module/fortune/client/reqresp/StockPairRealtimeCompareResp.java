package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-18
 */
@Data
public class StockPairRealtimeCompareResp implements Serializable {
    private Long stockPairTradeId;
    private StockTradePO buyTrade;
    private StockTradePO sellTrade;
    private BigDecimal currentBuyPrice;
    private BigDecimal currentSellPrice;
    private BigDecimal totalEarned;
    private BigDecimal totalFee;
    private boolean shouldTrade;
    private BigDecimal part1Earned;
    private BigDecimal part2Earned;
}
