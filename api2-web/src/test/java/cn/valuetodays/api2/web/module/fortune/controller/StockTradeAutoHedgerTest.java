package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AutoToHedgeTradeResp;
import cn.valuetodays.api2.module.fortune.service.kits.StockTradeAutoHedger;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StockTradeAutoHedger}.
 *
 * @author lei.liu
 * @since 2024-01-31
 */
@Slf4j
public class StockTradeAutoHedgerTest {

    private static StockTradePO ofBuy(Long id, String code, int quantity, double price) {
        return of(id, code, quantity, price, FortuneCommonEnums.TradeType.BUY);
    }

    private static StockTradePO ofSell(Long id, String code, int quantity, double price) {
        return of(id, code, quantity, price, FortuneCommonEnums.TradeType.SELL);
    }

    private static StockTradePO of(Long id, String code, int quantity, double price,
                                   FortuneCommonEnums.TradeType type) {
        StockTradePO po = new StockTradePO();
        po.setId(id);
        po.setCode(code);
        po.setQuantity(quantity);
        po.setPrice(BigDecimal.valueOf(price));
        po.setTradeType(type);
        po.setHedgeId(0L);
        return po;
    }

    @Test
    public void doAutoHedgeEmptyList() {
        callAndPrint(null);
    }

    @Test
    public void doAutoHedgeOnlyBuy() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(100L, "123", 1000, 0.5),
            ofBuy(91L, "123", 1000, 0.501)
        );
        callAndPrint(list);
    }

    @Test
    public void doAutoHedgeOnlySell() {
        List<StockTradePO> list = Arrays.asList(
            ofSell(100L, "123", 1000, 0.5),
            ofSell(91L, "123", 1000, 0.501)
        );
        callAndPrint(list);
    }

    @Test
    public void doAutoHedge1S1B() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(100L, "123", 1000, 0.5),
            ofSell(200L, "123", 1000, 0.501),
            ofBuy(91L, "123", 1000, 0.501),
            ofSell(88L, "123", 1000, 0.502)
        );
        callAndPrint(list);
    }

    @Test
    public void doAutoHedgeNS1B() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(100L, "123", 2000, 0.5),
            ofSell(200L, "123", 1000, 0.501),
            ofSell(88L, "123", 1000, 0.502)
        );
        callAndPrint(list);
    }

    @Test
    public void doAutoHedgeNS1B_With_NotMatched() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(100L, "123", 2000, 0.5),
            ofSell(200L, "123", 1000, 0.501),
            ofSell(88L, "123", 1000, 0.502),
            ofSell(89L, "123", 1000, 0.502)
        );
        callAndPrint(list);
    }

    @Test
    public void doAutoHedge1SNB() {
        List<StockTradePO> list = Arrays.asList(
            ofBuy(100L, "123", 1000, 0.501),
            ofBuy(101L, "123", 800, 0.502),
            ofBuy(103L, "123", 1200, 0.503),
            ofSell(88L, "123", 3000, 0.505)
        );
        callAndPrint(list);
    }

    @Test
    public void doAutoHedgeWrongList() {
        List<StockTradePO> list = Arrays.asList(
            ofSell(100L, "123", 1300, 0.555),
            ofBuy(101L, "123", 1300, 0.52),
            ofBuy(102L, "123", 1300, 0.53)
        );
        callAndPrint(list);
    }

    private void callAndPrint(List<StockTradePO> list) {
        AutoToHedgeTradeResp resp = new AutoToHedgeTradeResp();
        StockTradeAutoHedger autoHedger = new StockTradeAutoHedger();

        autoHedger.doAutoHedge(list, resp);
        log.info("resp={}", resp);
        if (Objects.nonNull(list)) {
            list.forEach(e -> {
                log.info("#{} -> {}", e.getId(), e.getHedgeId());
            });
        }
    }

}
