package cn.valuetodays.api2.module.fortune.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-02
 */
public class StockSubjectEnums {

    @Getter
    public enum Type implements TitleCapable {
        MINUTE_PRICE("分钟价格"),
        IOPV("iopv"),
        DAILY_PRICE("日K"),
        IN_OUT_STRATEGY("内外盘策略");

        private final String title;

        Type(String title) {
            this.title = title;
        }

    }
}
