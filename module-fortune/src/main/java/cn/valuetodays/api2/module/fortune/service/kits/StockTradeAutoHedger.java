package cn.valuetodays.api2.module.fortune.service.kits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.reqresp.AutoToHedgeTradeResp;
import cn.valuetodays.quarkus.commons.base.jpa.JpaIdBasePersist;
import org.apache.commons.collections4.CollectionUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-01-31
 */
public class StockTradeAutoHedger {

    public void doAutoHedge(List<StockTradePO> list, AutoToHedgeTradeResp resp) {
        if (CollectionUtils.isEmpty(list)) {
            resp.setMsg("no record(s)");
            return;
        }
        List<StockTradePO> buyTrades =
            StockTradeStatUtils.remainTradeWithBuy(list, Comparator.comparing(StockTradePO::getPrice));
        List<StockTradePO> sellTrades =
            StockTradeStatUtils.remainTradeWithSell(list, Comparator.comparing(StockTradePO::getPrice));
        if (CollectionUtils.isEmpty(buyTrades) || CollectionUtils.isEmpty(sellTrades)) {
            resp.setMsg("only buy trade(s) or only sell trade(s) or no trade(s)");
            return;
        }
        // 处理1卖1买匹配 开始
        // 优先匹配差价在0.001/0.002/0.003的交易（这种交易最多）
        // 这样可以避免一些本应匹配但未匹配的问题，如4笔交易：买1、卖10、买7、卖6，1和10匹配了，7和6就没法匹配了。实际应该是1配6、7配10，这样都能匹配上。
        doAutoHedge1S1B(buyTrades, sellTrades, BigDecimal.valueOf(0.001));
        doAutoHedge1S1B(buyTrades, sellTrades, BigDecimal.valueOf(0.002));
        doAutoHedge1S1B(buyTrades, sellTrades, BigDecimal.valueOf(0.003));
        doAutoHedge1S1B(buyTrades, sellTrades, BigDecimal.valueOf(0.004));
        doAutoHedge1S1B(buyTrades, sellTrades, BigDecimal.valueOf(0.005));
        doAutoHedge1S1B(buyTrades, sellTrades, null);
        // 处理1卖1买匹配 结束

        // 处理N卖1买匹配 开始
        doAutoHedgeNS1B(buyTrades, sellTrades, true);
        // 处理N卖1买匹配 结束

        // 处理1卖N买匹配 开始
        // 和 N卖1买 相反
        doAutoHedgeNS1B(sellTrades, buyTrades, false);
        // 处理1卖N买匹配 结束

        resp.setRecords(list.size());
        resp.setMatchedRecords((int) list.stream().filter(e -> e.getHedgeId() > 0L).count());

        final Collector<StockTradePO, ?, Map<Long, Long>> collector =
            Collectors.toMap(JpaIdBasePersist::getId, StockTradePO::getHedgeId);
        Map<Long, Long> newMap = list.stream().collect(collector);
        resp.setIdMap(
            newMap.entrySet().stream()
                .filter(e -> !Objects.equals(e.getValue(), 0L))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    private void doAutoHedgeNS1B(List<StockTradePO> buyTrades, List<StockTradePO> sellTrades, boolean doingBuy) {
        List<StockTradePO> buyTradesForNS1B = buyTrades.stream()
            .filter(StockTradePO::wasNotHedged)
            .toList();
        List<StockTradePO> sellTradesForNS1B = sellTrades.stream()
            .filter(StockTradePO::wasNotHedged)
            .toList();
        for (StockTradePO buyTrade : buyTradesForNS1B) {
            final String code = buyTrade.getCode();
            final int quantity = buyTrade.getQuantity();
            final BigDecimal price = buyTrade.getPrice();
            // 编号需要一样，卖的数量比买的数量少，且卖价比买价高
            List<StockTradePO> exactSellTrades = sellTradesForNS1B.stream()
                .filter(StockTradePO::wasNotHedged)
                .filter(
                    e -> e.getCode().equals(code) && e.getQuantity() < quantity
                )
                .filter(e -> doingBuy ? e.getPrice().compareTo(price) > 0 : e.getPrice().compareTo(price) < 0)
                .toList();

            if (CollectionUtils.isNotEmpty(exactSellTrades)) {
                int totalQuantityForSellTrades = exactSellTrades.stream()
                    .mapToInt(StockTradePO::getQuantity)
                    .sum();
                if (totalQuantityForSellTrades == quantity) {
                    hedgeRecord(buyTrade, exactSellTrades);
                } else if (totalQuantityForSellTrades > quantity) {
                    List<StockTradePO> candidateList = new ArrayList<>();
                    int availableQuantity = quantity;
                    for (StockTradePO e : exactSellTrades) {
                        int quantityInE = e.getQuantity();
                        availableQuantity -= quantityInE;
                        if (availableQuantity >= 0) {
                            candidateList.add(e);
                        }
                        if (availableQuantity == 0) {
                            break;
                        }
                    }
                    if (availableQuantity == 0) {
                        hedgeRecord(buyTrade, candidateList);
                    }
                }
            }
        }
    }

    private void doAutoHedge1S1B(List<StockTradePO> buyTrades, List<StockTradePO> sellTrades, BigDecimal offset) {
        // 买单按价格排序，优先从低价格开始匹配
        final List<StockTradePO> sortedBuyTrades = buyTrades.stream()
            .sorted(Comparator.comparing(StockTradePO::getPrice))
            .toList();
        for (StockTradePO buyTrade : sortedBuyTrades) {
            String code = buyTrade.getCode();
            int quantity = buyTrade.getQuantity();
            BigDecimal price = buyTrade.getPrice();
            // 编号、数量需要一样，且卖价比买价高
            // 若有多个，就取价格最小的那个
            StockTradePO exactSellTrade = sellTrades.stream()
                .filter(StockTradePO::wasNotHedged)
                .filter(
                    // 编号、数量需要一样，且卖价比买价高
                    e -> {
                        boolean conditionFix = e.getCode().equals(code) && e.getQuantity() == quantity;
                        if (Objects.nonNull(offset)) {
                            return conditionFix && e.getPrice().subtract(price).equals(offset);
                        } else {
                            return conditionFix && e.getPrice().compareTo(price) > 0;
                        }
                    }
                )
                .min(Comparator.comparing(StockTradePO::getPrice))
                .orElse(null);
            if (Objects.nonNull(exactSellTrade)) {
                hedgeRecord(buyTrade, exactSellTrade);
            }
        }
    }

    private void hedgeRecord(StockTradePO one, StockTradePO theOther) {
        one.setHedgeId(theOther.getId());
        theOther.setHedgeId(one.getId());
    }

    private void hedgeRecord(StockTradePO one, List<StockTradePO> theOthers) {
        one.setHedgeId(theOthers.get(0).getId());
        theOthers.forEach(e -> e.setHedgeId(one.getId()));
    }

}
