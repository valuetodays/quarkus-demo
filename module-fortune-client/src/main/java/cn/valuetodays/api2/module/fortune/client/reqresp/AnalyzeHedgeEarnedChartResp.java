package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-12
 */
@Data
public class AnalyzeHedgeEarnedChartResp {
    private List<String> dateList;
    private List<BigDecimal> earnedList;
}
