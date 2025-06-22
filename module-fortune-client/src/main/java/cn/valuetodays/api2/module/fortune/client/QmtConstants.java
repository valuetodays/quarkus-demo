package cn.valuetodays.api2.module.fortune.client;

import java.util.List;
import java.util.Map;

import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import lombok.Getter;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-23
 */
public class QmtConstants {

    public static final List<String> END_STRATEGY_LIST = List.of(StrategyType.PAIR_ME.name(),
        StrategyType.PAIR_AE.name(),
        StrategyType.OFFSET_AE.name());
    public static final List<String> AUTO_PAIR_NAMES = List.of(StrategyType.PAIR_AB.name(),
        StrategyType.PAIR_AE.name());
    public static final List<String> END_TRADES = List.of(
        StrategyType.PAIR_ME.name(), StrategyType.PAIR_AE.name(), StrategyType.PAIR_ME.name()
    );
    public static final int STOCK_BUY = 23;
    public static final int STOCK_SELL = 24;
    public static final Map<Integer, FortuneCommonEnums.TradeType> TRADE_TYPE_MAP =
        Map.of(
            STOCK_BUY, FortuneCommonEnums.TradeType.BUY,
            STOCK_SELL, FortuneCommonEnums.TradeType.SELL
        );
    // 委托状态
    //  未报
    public static final int ORDER_UNREPORTED = 48;
    //  待报
    public static final int ORDER_WAIT_REPORTING = 49;
    //  已报
    public static final int ORDER_REPORTED = 50;
    //  已报待撤
    public static final int ORDER_REPORTED_CANCEL = 51;
    //  部成待撤
    public static final int ORDER_PARTSUCC_CANCEL = 52;
    //  部撤
    public static final int ORDER_PART_CANCEL = 53;
    //  已撤
    public static final int ORDER_CANCELED = 54;
    //  部成
    public static final int ORDER_PART_SUCC = 55;
    //  已成
    public static final int ORDER_SUCCEEDED = 56;
    //  废单
    public static final int ORDER_JUNK = 57;
    //  未知
    public static final int ORDER_UNKNOWN = 255;
    @Getter
    public enum StrategyType {
        FIX("固定的单次交易"),
        PAIR_MB("手动pair开始，PAIR_MANUAL_BEGIN"),
        PAIR_ME("手动pair开始，PAIR_MANUAL_END"),

        PAIR_AB("自动pair开始，PAIR_AUTO_BEGIN"),
        PAIR_AE("自动pair结束，PAIR_AUTO_END"),

        OFFSET_AB("OFFSET开始，OFFSET_AUTO_BEGIN"),
        OFFSET_AE("OFFSET结束，OFFSET_AUTO_END"),
        ;
        private final String title;

        StrategyType(String title) {
            this.title = title;
        }
    }
    @Getter
    public enum TradeType {
        BUY("买"),
        SELL("卖");
        private final String title;

        TradeType(String title) {
            this.title = title;
        }
    }
}
