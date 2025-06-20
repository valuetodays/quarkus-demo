package cn.vt.rest.third.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.test.TestBase;
import cn.vt.util.YamlUtils;
import lombok.Data;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-10 11:26
 */
class StockTradeFeeUtilsTest extends TestBase {

    @Test
    void testEtfForComputeFee() {
        String yml = getFileAsString(this.getClass(), "etf-trade.yml");
        TradeRecords tradeRecordsObj = YamlUtils.toObject(yml, TradeRecords.class);
        List<TradeRecord> trades = tradeRecordsObj.getTrades();
        this.checkTradeDataForComputeFee(trades, TradeFeeConfConstants.ETF_TRADE_FEE_CONF);
    }

    @Test
    void testStockForComputeFee() {
        String yml = getFileAsString(this.getClass(), "stock-trade.yml");
        TradeRecords tradeRecordsObj = YamlUtils.toObject(yml, TradeRecords.class);
        List<TradeRecord> trades = tradeRecordsObj.getTrades();
        this.checkTradeDataForComputeFee(trades, TradeFeeConfConstants.STOCK_TRADE_FEE_CONF);
    }

    private void checkTradeDataForComputeFee(List<TradeRecord> tradeRecordList, ITradeFeeConf tradeFeeConf) {
        for (int i = 0; i < tradeRecordList.size(); i++) {
            TradeRecord arg = tradeRecordList.get(i);
            String code = arg.getCode();
            BigDecimal quantity = BigDecimal.valueOf(arg.getQuantity());
            BigDecimal price = arg.getPrice();
            BigDecimal fee;
            boolean buy = arg.buy;
            if (buy) {
                fee = StockTradeFeeUtils.computeFeeForBuy(price, quantity, code, tradeFeeConf);
            } else {
                fee = StockTradeFeeUtils.computeFeeForSell(price, quantity, code, tradeFeeConf);
            }
            BigDecimal yongjinFee = StockTradeFeeUtils.computeYongJinFee(price, quantity, tradeFeeConf);
            assertBigDecimalEquals("yongjinFee #" + i, yongjinFee, arg.getYongjinFee());
            FortuneCommonEnums.TradeType tradeType = buy ? FortuneCommonEnums.TradeType.BUY : FortuneCommonEnums.TradeType.SELL;
            BigDecimal yinhuaFee = StockTradeFeeUtils.computeYinhuaFee(price, quantity, tradeType, tradeFeeConf);
            assertBigDecimalEquals("yinhuaFee #" + i, yinhuaFee, arg.getYinhuaFee());
            BigDecimal guohuFee = StockTradeFeeUtils.computeGuohuFee(price, quantity, code, tradeFeeConf);
            assertBigDecimalEquals("guohuFee #" + i, guohuFee, arg.getGuohuFee());
        }
    }

    private void assertBigDecimalEquals(String tag, BigDecimal actual, BigDecimal expected) {
        assertThat(
            tag,
            actual.setScale(2, RoundingMode.HALF_UP),
            CoreMatchers.equalTo(expected.setScale(2, RoundingMode.HALF_UP))
        );
    }

    @Data
    private static class TradeRecord {
        private boolean buy;
        private String code;
        private int quantity;
        private BigDecimal price;
        private BigDecimal yongjinFee;
        private BigDecimal yinhuaFee;
        private BigDecimal guohuFee;
    }

    @Data
    private static class TradeRecords {
        private List<TradeRecord> trades;
    }

}
