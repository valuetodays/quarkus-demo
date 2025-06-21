package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * 指数统计结果.
 *
 * @author lei.liu
 * @since 2023-05-12 12:46
 */
@Data
public class DailyOffsetStatResp implements Serializable {
    // 当日最高价与最低低差值的最大值、最小值、平均值
    private double max;
    private double min;
    private double average;
    private double higherLine;
    private double higherLinePtg;
    private double lowerLine;
    private double lowerLinePtg;
    // 日内差值与历史上所有大于等于此值的天数占总天数的百分比
    private Map<Double, Double> offsetAndHistoryPtg;

    // 统计标题
    private String statTitle;

}
