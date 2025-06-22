package cn.valuetodays.api2.module.fortune.client.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-10
 */
public final class StockTradeMonitorEnums {

    @Getter
    public enum Status implements TitleCapable {
        NORMAL("正常"),
        TEMP_HIDDEN("临时隐藏"),
        CLOSED("已关闭");

        private final String title;

        Status(String title) {
            this.title = title;
        }
    }

}
