package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.util.PriceUtilsEx;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums.TradeType;
import io.smallrye.mutiny.tuples.Tuple5;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Tests for {@link PriceUtilsEx}.
 *
 * @author lei.liu
 * @since 2025-02-05
 */
class PriceUtilsExTest {
    private static final BigDecimal TOLERANCE = new BigDecimal("0.00000001");

    @Test
    void calcOppositePrice() {
        List<Tuple5<String, TradeType, Integer, BigDecimal, BigDecimal>> tupleList = List.of(
            Tuple5.of("513360", TradeType.BUY, 1500, BigDecimal.valueOf(0.800), BigDecimal.valueOf(0.801)),
            Tuple5.of("513360", TradeType.SELL, 2700, BigDecimal.valueOf(0.800), BigDecimal.valueOf(0.799)),
            Tuple5.of("513360", TradeType.SELL, 200, BigDecimal.valueOf(0.800), BigDecimal.valueOf(0.798)),
            Tuple5.of("600036", TradeType.BUY, 1000, BigDecimal.valueOf(35.35), BigDecimal.valueOf(35.45)),
            Tuple5.of("159545", TradeType.SELL, 100, BigDecimal.valueOf(1.209), BigDecimal.valueOf(1.205))
        );
        for (Tuple5<String, TradeType, Integer, BigDecimal, BigDecimal> tuple : tupleList) {
            StockTradePO trade = new StockTradePO();
            trade.setCode(tuple.getItem1());
            trade.setTradeType(tuple.getItem2());
            trade.setQuantity(tuple.getItem3());
            trade.setPrice(tuple.getItem4());
            BigDecimal p = PriceUtilsEx.calcOppositePrice(trade);
            assertThat(p, closeTo(tuple.getItem5(), TOLERANCE));
        }
    }
}
