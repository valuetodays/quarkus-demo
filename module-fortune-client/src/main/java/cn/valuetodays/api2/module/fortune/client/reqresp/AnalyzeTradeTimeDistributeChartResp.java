package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-12
 */
@Data
public class AnalyzeTradeTimeDistributeChartResp {
    private List<CountByTime> list;

    @Data
    public static class CountByTime implements Serializable {
        private String time;
        private Integer c;
    }
}
