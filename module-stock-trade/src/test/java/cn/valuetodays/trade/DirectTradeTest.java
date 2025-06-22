package cn.valuetodays.trade;

import java.util.ArrayList;
import java.util.List;

import cn.vt.rest.third.utils.StockCodeUtils;
import lombok.Data;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-11
 */
@Disabled
public class DirectTradeTest {
    private final HaitongHttpAutoTrade haitongHttpAutoTrade = new HaitongHttpAutoTrade();

    private void doTradeOne(DirectTradeTest.TradeVo tradeVo) {
        String type = tradeVo.getType();
        String code = tradeVo.getCode();
        String codeToUse = StockCodeUtils.buildForEhaifangzhou(code);
        if (TradeVo.TYPE_BUY.equals(type)) {
            haitongHttpAutoTrade.doBuy(codeToUse, tradeVo.getPrice(), tradeVo.getQuantity(), "");
        } else {
            haitongHttpAutoTrade.doSell(codeToUse, tradeVo.getPrice(), tradeVo.getQuantity(), "");
        }
    }

    @Test
    public void doTrade() {
        List<TradeVo> toTradeList = new ArrayList<>();
        toTradeList.add(TradeVo.ofSell("518880", 5.3680, 4100));
        toTradeList.add(TradeVo.ofSell("513360", 0.573, 26800));
        toTradeList.add(TradeVo.ofSell("159992", 0.6880, 3100));
        toTradeList.add(TradeVo.ofSell("560350", 0.9990, 6000));
        toTradeList.add(TradeVo.ofSell("513330", 0.3560, 30000));
        toTradeList.clear();
        for (TradeVo td : toTradeList) {
            doTradeOne(td);
        }
    }

    @Data
    private static class TradeVo {
        public static final String TYPE_BUY = "BUY";
        public static final String TYPE_SELL = "SELL";
        private String code;
        private double price;
        private int quantity;
        private String type;

        public static TradeVo ofBuy(String code, double price, int quantity) {
            return of(code, price, quantity, TYPE_BUY);
        }

        public static TradeVo ofSell(String code, double price, int quantity) {
            return of(code, price, quantity, TYPE_SELL);
        }

        public static TradeVo of(String code, double price, int quantity, String type) {
            TradeVo tradeVo = new TradeVo();
            tradeVo.setCode(code);
            tradeVo.setPrice(price);
            tradeVo.setQuantity(quantity);
            tradeVo.setType(type);
            return tradeVo;
        }
    }
}
