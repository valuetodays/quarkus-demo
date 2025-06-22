package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-01
 */
@Data
public class SaveUpStockReq implements Serializable {
    private BigDecimal buyPrice;
    private Integer buyQuantity;
    private Category category;

    @Getter
    public enum Category {
        AUTO(BigDecimal.ZERO),
        STOCK(BigDecimal.valueOf(0.01)),
        ETF(BigDecimal.valueOf(0.001));

        private final BigDecimal offsetValue;

        Category(BigDecimal offsetValue) {
            this.offsetValue = offsetValue;
        }
    }
}
