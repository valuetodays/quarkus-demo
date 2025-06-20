package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-12
 */
@Data
public class T0DailyChartResp implements Serializable {
    private Integer statDate;
    private BigDecimal huan_shou_ptg;
    private BigDecimal yi_jia_ptg;
    private BigDecimal open_px;
    private BigDecimal close_px;
    private BigDecimal high_px;
    private BigDecimal low_px;
    private BigDecimal value;
    private BigDecimal inner_pan_wan;
    private BigDecimal outer_pan_wan;
    private BigDecimal liangbi;
}
