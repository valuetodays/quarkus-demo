package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.util.PriceUtilsEx;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class StockTradeOrderVo implements Serializable {
    private Long id;
    private FortuneCommonEnums.TradeType tradeType;
    private String code;
    private int quantity;
    private BigDecimal price;
    private Long hedgeId = 0L;
    private String remark;
    private BigDecimal amount;
    private BigDecimal amountWithSign;
    private BigDecimal fee;

    public boolean wasNotHedged() {
        return Objects.isNull(hedgeId) || hedgeId.equals(0L);
    }

    public boolean wasHedged() {
        return !wasNotHedged();
    }

    @JsonIgnore
    public void compute() {
        BigDecimal price_d = PriceUtilsEx.fixPrice(price);
        this.amount = price_d.multiply(BigDecimal.valueOf(quantity));
        if (FortuneCommonEnums.TradeType.BUY == tradeType) {
            this.amountWithSign = this.amount.negate();
        } else {
            this.amountWithSign = this.amount;
        }
        // # 手续费是万0.5，最低0.1
        BigDecimal fee_should_be = amount
            .multiply(new BigDecimal("0.5"))
            .divide(new BigDecimal("10000.0"), 2, RoundingMode.UP);
        this.fee = new BigDecimal("0.1").max(fee_should_be);
    }

}
