package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-05-18
 */
@Data
public class IndustryDailyStatDaysReq implements Serializable {
    private int days;
}
