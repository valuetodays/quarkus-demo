package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeHedgedTradeResp;
import cn.valuetodays.api2.module.fortune.service.module.StockTradeEarnComputeModule;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StockTradeEarnComputeModule}.
 *
 * @author lei.liu
 * @since 2023-11-04
 */
@Slf4j
public class StockTradeEarnComputeModuleIT {
    private final StockTradeEarnComputeModule module = new StockTradeEarnComputeModule();

    /**
     * 冰哥策略
     */
    @Test
    public void doComputeEarn_2vs2() {
        List<StockTradePO> hedgedList = List.of(
            buildBuyPO(100L, 101L, "", 2600, BigDecimal.valueOf(22.03),
                BigDecimal.valueOf(11.7000), BigDecimal.valueOf(0.0000), BigDecimal.valueOf(0.8600)),
            buildSellPO(101L, 200L, "", 1700, BigDecimal.valueOf(33.49),
                BigDecimal.valueOf(9.9900), BigDecimal.valueOf(43.2800), BigDecimal.valueOf(0.8700)),

            buildBuyPO(200L, 201L, "", 1700, BigDecimal.valueOf(33.65),
                BigDecimal.valueOf(11.7000), BigDecimal.valueOf(0.0000), BigDecimal.valueOf(0.8600)),
            buildSellPO(201L, 100L, "", 2600, BigDecimal.valueOf(22.33),
                BigDecimal.valueOf(9.9900), BigDecimal.valueOf(43.2800), BigDecimal.valueOf(0.8700))

        );
        AnalyzeHedgedTradeResp resp = module.doComputeEarn(hedgedList);
        List<AnalyzeHedgedTradeResp.Item> itemList = resp.getItemList();
        itemList.forEach(e -> log.info(":: {}", e));
    }

    @Test
    public void doComputeEarn_1vs1() {
        List<StockTradePO> hedgedList = Arrays.asList(
            buildBuyPO(389L, 388L, "", 15000, BigDecimal.valueOf(5.7600),
                BigDecimal.valueOf(11.7000), BigDecimal.valueOf(0.0000), BigDecimal.valueOf(0.8600)),
            buildSellPO(388L, 389L, "", 15000, BigDecimal.valueOf(5.7700),
                BigDecimal.valueOf(9.9900), BigDecimal.valueOf(43.2800), BigDecimal.valueOf(0.8700))
        );
        AnalyzeHedgedTradeResp resp = module.doComputeEarn(hedgedList);
        List<AnalyzeHedgedTradeResp.Item> itemList = resp.getItemList();
        itemList.forEach(e -> log.info(":: {}", e));
    }

    private StockTradePO buildBuyPO(Long id, Long hedgeId, String code, int quantity, BigDecimal price,
                                    BigDecimal yongjinFee, BigDecimal yinhuaFee, BigDecimal guohuFee) {
        return build(id, hedgeId, FortuneCommonEnums.TradeType.BUY, code,
            quantity, price, yongjinFee, yinhuaFee, guohuFee);
    }

    private StockTradePO buildSellPO(Long id, Long hedgeId, String code, int quantity, BigDecimal price,
                                     BigDecimal yongjinFee, BigDecimal yinhuaFee, BigDecimal guohuFee) {
        return build(id, hedgeId, FortuneCommonEnums.TradeType.SELL, code,
            quantity, price, yongjinFee, yinhuaFee, guohuFee);
    }

    private StockTradePO build(Long id, Long hedgeId, FortuneCommonEnums.TradeType tradeType,
                               String code, int quantity, BigDecimal price,
                               BigDecimal yongjinFee, BigDecimal yinhuaFee, BigDecimal guohuFee) {
        StockTradePO po = new StockTradePO();
        po.setCode(code);
        po.setTradeType(tradeType);
        po.setQuantity(quantity);
        po.setPrice(price);
        po.setYongjinFee(yongjinFee);
        po.setYinhuaFee(yinhuaFee);
        po.setGuohuFee(guohuFee);
        po.setHedgeId(hedgeId);
        po.setId(id);
        return po;
    }

}
