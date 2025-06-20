package cn.valuetodays.api2.module.fortune.reqresp.offset;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StockBiasOffset extends DoubleOffset {
    public StockBiasOffset() {
    }

    public StockBiasOffset(Double value, Double avg) {
        super(value, avg);
    }
}
