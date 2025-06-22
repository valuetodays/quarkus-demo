package cn.valuetodays.trade;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * 证券提供商.
 *
 * @author lei.liu
 * @since 2023-05-27 15:06
 */
@Getter
public enum QsProvider implements TitleCapable {
    HUATAI("华泰"),
    HUABAO("华宝"),
    HAITONG("海通");

    private final String title;

    QsProvider(String title) {
        this.title = title;
    }
}
