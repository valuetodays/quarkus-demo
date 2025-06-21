package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.service.kits.CheckHedgedKits;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CheckHedgedKits}.
 *
 * @author lei.liu
 * @since 2024-02-03
 */
public class CheckHedgedKitsTest {

    private static StockTradePO ofBuy(Long id, Long hedgeId, int quantity) {
        return of(id, hedgeId, quantity, FortuneCommonEnums.TradeType.BUY);
    }

    private static StockTradePO ofSell(Long id, Long hedgeId, int quantity) {
        return of(id, hedgeId, quantity, FortuneCommonEnums.TradeType.SELL);
    }

    private static StockTradePO of(Long id, Long hedgeId, int quantity,
                                   FortuneCommonEnums.TradeType type) {
        StockTradePO po = new StockTradePO();
        po.setId(id);
        po.setCode("abc");
        po.setQuantity(quantity);
        po.setPrice(BigDecimal.valueOf(1));
        po.setTradeType(type);
        po.setHedgeId(hedgeId);
        return po;
    }

    @Test
    public void doCheck() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(100L, 91L, 1000),
            ofSell(91L, 100L, 1000)
        );

        CheckHedgedKits kits = new CheckHedgedKits();
        kits.doCheck(list);
    }

    @Test
    public void doCheck2VS1() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(594L, 596L, 1000),
            ofBuy(595L, 596L, 1000),
            ofSell(596L, 595L, 2000)
        );

        CheckHedgedKits kits = new CheckHedgedKits();
        kits.doCheck(list);
    }
}
