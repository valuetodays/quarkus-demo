package cn.valuetodays.trade;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-27 16:07
 */
@Data
public class AccountInfo implements Serializable {
    // 可用金额
    private BigDecimal availableUseMoney;
    // 可取金额
    private BigDecimal availableWithdrawMoney;
    // 冻结金额
    private BigDecimal freezeMoney;
    // 股票市值金额
    private BigDecimal stockMarketMoney;
    // 总资产
    private BigDecimal totalMoney;

}
