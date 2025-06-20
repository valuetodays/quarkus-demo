package cn.valuetodays.api2.module.fortune.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2022-09-22
 */
public final class StockHistoryPriceEnums {

    @Getter
    public enum Channel implements TitleCapable {
        CS("中证网"),
        QMT("QMT"),
        XUEQIU("雪球");

        private final String title;

        Channel(String title) {
            this.title = title;
        }
    }

}
