package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成交的委托.
 *
 * @author lei.liu
 * @see OrderInfo
 * @since 2024-05-31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DealedOrderInfo extends OrderInfo {
    private BigDecimal yjFee;
    private BigDecimal yhFee = BigDecimal.ZERO;
    private BigDecimal ghFee = BigDecimal.ZERO;
}
