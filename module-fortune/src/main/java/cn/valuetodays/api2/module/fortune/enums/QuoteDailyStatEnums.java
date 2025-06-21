package cn.valuetodays.api2.module.fortune.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-02 13:22
 */
public final class QuoteDailyStatEnums {
    @Getter
    public enum Channel implements TitleCapable {
        CS("中证网"),
        XUEQIU("雪球"),
        EASTMONEY("东方财富"),
        SSE("上交所"),
        ;

        private final String title;

        Channel(String title) {
            this.title = title;
        }

    }
}
