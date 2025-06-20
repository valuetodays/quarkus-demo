package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-11-23
 */
@Data
public class StatEarnedMonthlyResp implements Serializable {
    private String yearMonthStr;
    private BigDecimal money;
}
