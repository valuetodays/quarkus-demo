package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 指数统计结果.
 *
 * @author lei.liu
 * @since 2023-05-10 15:46
 */
@Data
public class QuoteStatResp implements Serializable {
    // 期初点数
    private double openPx;
    private double ptg;
    // 当前收盘点数
    private double currentClosePx;
    // 期间最高点数
    private double highestPx;
    private double ptgFromHighest;
    // 期间最低点数
    private double lowestPx;
    private double ptgFromLowest;
    // 统计开始时间 yyyyMMdd
    private int beginStatMinTime;
    // 统计结束时间 yyyyMMdd
    private int endStatMinTime;
    // 统计标题
    private String statTitle;

    @JsonIgnore
    public void computePercentage() {
        final BigDecimal const100 = BigDecimal.valueOf(100);

        BigDecimal openPxNum = BigDecimal.valueOf(openPx);
        ptg = BigDecimal.valueOf(currentClosePx).subtract(openPxNum)
            .multiply(const100)
            .divide(openPxNum, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP).doubleValue();

        BigDecimal highestPxNum = BigDecimal.valueOf(highestPx);
        ptgFromHighest = BigDecimal.valueOf(currentClosePx).subtract(highestPxNum)
            .multiply(const100)
            .divide(highestPxNum, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP).doubleValue();

        BigDecimal lowestPxNum = BigDecimal.valueOf(lowestPx);
        ptgFromLowest = BigDecimal.valueOf(currentClosePx).subtract(lowestPxNum)
            .multiply(const100)
            .divide(lowestPxNum, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
