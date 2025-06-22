package cn.valuetodays.api2.web.basic.push.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-05-24
 */
public final class NotifyEnums {
    @Getter
    public enum Group implements TitleCapable {
        CAPITAL("资本市场"),
        XUEQIU_IMAGE("雪球图片"),
        WARN("警告"),
        CD_CI("CD/CI"),
        TEST("测试"),
        APPLICATION_EXCEPTION("应用异常"),
        APPLICATION_MSG("应用消息"),
        API_ALARM("api告警");

        private final String title;

        Group(String title) {
            this.title = title;
        }
    }
}
