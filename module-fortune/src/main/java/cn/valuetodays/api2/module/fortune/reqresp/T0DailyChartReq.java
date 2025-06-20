package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.Set;

import cn.vt.core.TitleCapable;
import lombok.Data;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-12
 */
@Data
public class T0DailyChartReq implements Serializable {
    private String code;
    private Set<MetricType> metricTypes;

    @Getter
    public enum MetricType implements TitleCapable {
        HUAN_SHOU_PTG("换手率", "huan_shou_ptg"),
        YI_JIA_PTG("溢价率", "yi_jia_ptg"),
        OPEN_PX("开盘价", "open_px"),
        CLOSE_PX("收盘价", "close_px"),
        HIGH_PX("最高价", "high_px"),
        LOW_PX("最低价", "low_px"),
        INNER_PAN_WAN("内盘", "inner_pan_wan"),
        OUTER_PAN_WAN("外盘", "outer_pan_wan"),
        LIANGBI("量比", "liangbi");

        private final String title;
        private final String columnName;

        MetricType(final String title, String columnName) {
            this.title = title;
            this.columnName = columnName;
        }
    }
}
