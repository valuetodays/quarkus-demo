package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-03 20:37
 */
@Data
public class AShareHistoryTurnAmountResp implements Serializable {
    private List<DailyAmountData> list;

    @Data
    public static class DailyAmountData implements Serializable {
        private String minTime;
        private Long turnAmount;

    }

}
