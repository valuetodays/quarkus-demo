package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-29
 */
@Data
public class AnalyzeHedgedTradeResp implements Serializable {
    private BigDecimal totalEarned;  // 含手续费的总额
    private BigDecimal totalNetEarnedCash; // 不含手续费的总额
    private BigDecimal totalCommission; // 手续费总额
    private List<Item> itemList;
    private int tradedDays;
    private int tradedDaysWithWeekends;

    @Data
    public static class Item implements Serializable {
        // 对冲id
        private String hedgeIds;
        // 对冲id对应的对手ids
        private String vsIds;
        // 赚得的现金数(含手续费)
        private BigDecimal earned;
        // 赚得的现金数
        private BigDecimal netEarnedCash;
        // 手续费
        private BigDecimal commission;
        // 赚得的股票数
        private Integer earnedStockQuantity;
        // 错误日志
        private String errorMsg;

        @JsonIgnore
        public void computeErrorMsg() {
            if (StringUtils.isNotBlank(this.errorMsg)) {
                return;
            }
            String msg = StringUtils.EMPTY;
            if (netEarnedCash.compareTo(BigDecimal.ZERO) <= 0) {
                msg += ("赔钱的交易，【" + hedgeIds + "vs" + vsIds + "】");
            }
            BigDecimal subtract = earned.subtract(netEarnedCash.add(commission));
            if (subtract.compareTo(BigDecimal.ONE.negate()) <= 0) {
                msg += ("总额 != 手续费 + 净赚");
            }
            this.errorMsg = StringUtils.trimToNull(msg);
        }
    }
}
