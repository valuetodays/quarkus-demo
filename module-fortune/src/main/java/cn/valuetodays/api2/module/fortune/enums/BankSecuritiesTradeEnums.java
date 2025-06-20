package cn.valuetodays.api2.module.fortune.enums;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-19
 */
public final class BankSecuritiesTradeEnums {
    @Getter
    public enum Direction implements TitleCapable {
        B2S("银行到证券"),
        S2B("证券到银行");

        private final String title;

        Direction(String title) {
            this.title = title;
        }
    }
}
