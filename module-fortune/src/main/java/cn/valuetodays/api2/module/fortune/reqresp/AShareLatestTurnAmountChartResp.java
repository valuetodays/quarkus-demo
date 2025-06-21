package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-30
 */
@Data
public class AShareLatestTurnAmountChartResp implements Serializable {
    private List<String> minTimeList;
    private List<Long> amountByYiList;
}
