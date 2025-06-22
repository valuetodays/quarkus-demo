package cn.valuetodays.trade;

import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-25 20:21
 */
@Data
public class TradeConfProperties {
    private Flags flags = new Flags();
    private String strategyApiUrl;
    private String strategyT0ExchangeApiUrl;
    private int minMoneyPerTrade;
    private double minNetProfit;
    private QsProvider qsProvider;
    private StrategyT0 strategyT0;

    @Data
    public static class StrategyT0 {
        private String id;
        private String title;
        private List<EtfGroup> etfGroups;

        @Data
        public static class EtfGroup {
            private String title;
            private List<String> codes;
        }
    }

    @Data
    public static class Flags {
        private boolean dryRun;
        // 交易前保存窗口截图 - 开关
        private boolean saveScreenshotBeforeTrade;
        // 使用快捷键进入交易页面 - 开关  . 需要和 Positions 一起使用
        // 使用快捷键的问题是：可能按键已被其它软件绑定
        private boolean useHotKeyToEnterTradePage;
        // true 先买后卖； false 先卖后买
        private boolean buyFirst;
    }


}
