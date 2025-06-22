package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-09-24
 */
@Data
public class AccountProfitVo implements Serializable {
    private Integer statDate;
    private BigDecimal profit;
}
