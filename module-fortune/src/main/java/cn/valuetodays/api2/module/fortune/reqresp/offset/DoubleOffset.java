package cn.valuetodays.api2.module.fortune.reqresp.offset;

import java.math.BigDecimal;

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
public class DoubleOffset extends BaseOffset<Double> {
    public DoubleOffset() {
    }

    public DoubleOffset(Double value, Double avg) {
        super(value, avg);
    }

    @Override
    public void computeOffset() {
        double v = BigDecimal.valueOf(getValue()).subtract(BigDecimal.valueOf(getAvg())).doubleValue();
        setOffset(v);
    }
}
