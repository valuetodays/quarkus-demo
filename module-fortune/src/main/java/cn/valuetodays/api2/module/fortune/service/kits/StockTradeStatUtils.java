package cn.valuetodays.api2.module.fortune.service.kits;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import cn.valuetodays.api2.module.fortune.persist.StockTradePO;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import org.apache.commons.collections4.CollectionUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-03
 */
public final class StockTradeStatUtils {
    private static final List<FortuneCommonEnums.TradeType> BUY_TYPES = Arrays.asList(
        FortuneCommonEnums.TradeType.BUY, FortuneCommonEnums.TradeType.RONG_BUY
    );
    private static final List<FortuneCommonEnums.TradeType> SELL_TYPES = Arrays.asList(
        FortuneCommonEnums.TradeType.SELL, FortuneCommonEnums.TradeType.SELL_FOR_REPAY
    );

    private StockTradeStatUtils() {
    }

    /**
     * 判断同一股票的一组交易是否能够完全对冲
     *
     * @param tradesWithSameCode 同一股票的一组交易
     */
    public static boolean canBeTotallyHedged(Set<StockTradePO> tradesWithSameCode) {
        if (CollectionUtils.isEmpty(tradesWithSameCode)) {
            return false;
        }
        if (tradesWithSameCode.stream().map(StockTradePO::getCode).distinct().count() != 1) {
            return false;
        }
        List<StockTradePO> buyTrades = remainTradeWithBuy(tradesWithSameCode, null);
        List<StockTradePO> sellTrades = remainTradeWithSell(tradesWithSameCode, null);
        if (CollectionUtils.isEmpty(buyTrades) || CollectionUtils.isEmpty(sellTrades)) {
            return false;
        }

        int totalQuantityForBuyTrades = buyTrades.stream()
            .mapToInt(StockTradePO::getQuantity)
            .sum();
        int totalQuantityForSellTrades = sellTrades.stream()
            .mapToInt(StockTradePO::getQuantity)
            .sum();
        return totalQuantityForBuyTrades == totalQuantityForSellTrades;
    }

    public static List<StockTradePO> remainTrade(Collection<StockTradePO> trades,
                                                 List<FortuneCommonEnums.TradeType> tradeTypes,
                                                 Comparator<StockTradePO> comparator) {
        Stream<StockTradePO> stream = trades.stream()
            .filter(e -> tradeTypes.contains(e.getTradeType()));
        if (Objects.nonNull(comparator)) {
            stream = stream.sorted(comparator);
        }
        return stream.toList();
    }

    public static List<StockTradePO> remainTradeWithBuy(Collection<StockTradePO> trades,
                                                        Comparator<StockTradePO> comparator) {
        return remainTrade(trades, BUY_TYPES, comparator);
    }

    public static List<StockTradePO> remainTradeWithSell(Collection<StockTradePO> trades,
                                                         Comparator<StockTradePO> comparator) {
        return remainTrade(trades, SELL_TYPES, comparator);
    }

}
