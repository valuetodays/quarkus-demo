package cn.valuetodays.api2.module.fortune.client.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-04-16
 */
public final class StockAlertEnums {
    @Getter
    public enum ScheduleType implements TitleCapable {
        CLOSE("收盘"),
        EVERY_10_MIN("每10分钟"),
        EVERY_20_MIN("每20分钟");

        private final String title;

        ScheduleType(String title) {
            this.title = title;
        }
    }

    @Getter
    public enum Status implements TitleCapable {
        NORMAL("正常"),
        CLOSED("已关闭");

        private final String title;

        Status(String title) {
            this.title = title;
        }
    }

    @Getter
    public enum CodeType implements TitleCapable {
        STOCK("股票"),
        INDEX("指数");

        private final String title;

        CodeType(String title) {
            this.title = title;
        }
    }
}
