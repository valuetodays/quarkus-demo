package cn.valuetodays.api2.module.fortune.reqresp.offset;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-26
 */
@Data
public abstract class BaseOffset<T extends Number> implements Serializable {
    private T value;
    private T avg;
    private Double offset;
    private Double bias;

    public BaseOffset() {
    }

    public BaseOffset(T value, T avg) {
        this.value = value;
        this.avg = avg;
        this.computeOffset();
        this.computeBias();
    }

    @JsonIgnore
    public abstract void computeOffset();

    @JsonIgnore
    public void computeBias() {
        if (Objects.isNull(value) || Objects.isNull(avg)) {
            throw new IllegalArgumentException("'value' and 'avg' should not be null.");
        }
        BigDecimal valueBd = new BigDecimal(value.toString());
        BigDecimal avgBd = new BigDecimal(avg.toString());
        BigDecimal biasBd = valueBd
            .subtract(avgBd)
            .divide(avgBd, 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)); // 百分比
        this.bias = biasBd.doubleValue();
    }
}
