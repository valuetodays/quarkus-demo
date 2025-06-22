package cn.valuetodays.api2.module.fortune.client.enums;

import java.util.Arrays;

import cn.vt.core.TitleCapable;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-30
 */
public final class FundTradeMonthlyStatEnums {
    @Getter
    public enum Channel implements TitleCapable {
        SSE("上交所"),
        ;

        private final String title;

        Channel(String title) {
            this.title = title;
        }
    }

    @Getter
    public enum Type implements TitleCapable {
        FUND("基金", "05", "18"),
        ETF("ETF", "13", "22"),
        REITS("REITS", "16", "49"),
        LOF("LOF", "14", "35"),
        UNKNOWN("unknown", "", "");

        private final String title;
        private final String sseBizCode;
        private final String sseBizHistoryCode;

        Type(String title, String sseBizCode, String sseBizHistoryCode) {
            this.title = title;
            this.sseBizCode = sseBizCode;
            this.sseBizHistoryCode = sseBizHistoryCode;
        }

        public static Type byCode(String code) {
            // 避免返回null
            return Arrays.stream(Type.values())
                .filter(e -> e.sseBizCode.equals(code))
                .findFirst().orElse(UNKNOWN);
        }

        public static Type byHistoryCode(String historyCode) {
            // 避免返回null
            return Arrays.stream(Type.values())
                .filter(e -> e.sseBizHistoryCode.equals(historyCode))
                .findFirst().orElse(UNKNOWN);
        }
    }
}
