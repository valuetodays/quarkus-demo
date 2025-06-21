package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * A股每日成交额.
 *
 * @author lei.liu
 * @since 2023-04-03 20:48
 */
@Data
public class AShareDailyTurnAmount implements Serializable {
    private Integer minTime;
    private Long turnAmount;
}
