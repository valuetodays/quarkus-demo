package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.reqresp.EtfsCompareResp;
import cn.vt.rest.third.utils.StockTradeFeeUtils;
import cn.vt.rest.third.utils.StockUtils;
import cn.vt.rest.third.utils.TradeFeeConfConstants;
import cn.vt.rest.third.xueqiu.StockRealtimeQuoteComponent;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteData;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 雪球股票实时报价-高级使用场景.
 *
 * @author lei.liu
 * @see StockRealtimeQuoteComponent 雪球股票实时报价-基础功能
 * @since 2023-05-23 15:05
 */
@ApplicationScoped
@Slf4j
public class StockRealtimeQuoteAdvanceScenarioComponent {

    public EtfsCompareResp processCompare(String group, int tn,
                                          List<String> etfList, final int minMoneyPerTrade) {
        List<XueQiuStockRealtimeQuoteData> dataList = StockRealtimeQuoteComponent.doGet(etfList);
        List<EtfsCompareResp.Result> infos = new ArrayList<>();
        List<EtfsCompareResp.Suggest> suggests = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            XueQiuStockRealtimeQuoteData quote = dataList.get(i);
            String symbol1 = quote.getSymbol();
            Double open1 = quote.getOpen();
            Double current1 = quote.getCurrent();
            EtfsCompareResp.Result info = new EtfsCompareResp.Result();
            info.setCode(symbol1);
            info.setOpen(open1);
            info.setCurrent(current1);
            info.setAvgPrice(quote.getAvgPrice());
            infos.add(info);
            for (int j = i + 1; j < dataList.size(); j++) {
                XueQiuStockRealtimeQuoteData next = dataList.get(j);
                String symbol2 = next.getSymbol();
                Double open2 = next.getOpen();
                Double current2 = next.getCurrent();
                EtfsCompareResp.Suggest buySellTip = computeBuySellSuggestion(
                    minMoneyPerTrade,
                    symbol1, open1, current1,
                    symbol2, open2, current2
                );
                if (Objects.nonNull(buySellTip)) {
                    suggests.add(buySellTip);
                }
            }
        }
        EtfsCompareResp etfsCompareResp = new EtfsCompareResp();
        etfsCompareResp.setGroupName(group);
        etfsCompareResp.setTn(tn);
        etfsCompareResp.setInfos(infos);
        etfsCompareResp.setSuggests(suggests);
        return etfsCompareResp;
    }

    /**
     * 计算买卖建议
     *
     * @param minMoneyPerTrade 每次交易的最小金额
     */
    public EtfsCompareResp.Suggest computeBuySellSuggestion(final int minMoneyPerTrade,
                                                            String symbol1, Double open1, Double current1,
                                                            String symbol2, Double open2, Double current2) {
        // 没有价格，一般是停牌
        if (!ObjectUtils.allNotNull(open1, current1, open2, current2)) {
            return null;
        }
        BigDecimal chgRadio1 = BigDecimal.valueOf(current1).divide(BigDecimal.valueOf(open1), 5, RoundingMode.DOWN);
        BigDecimal chgRadio2 = BigDecimal.valueOf(current2).divide(BigDecimal.valueOf(open2), 5, RoundingMode.DOWN);
        if (chgRadio2.compareTo(chgRadio1) == 0) {
            return null;
        } else if (chgRadio2.compareTo(chgRadio1) > 0) {
            return computeBuySellSuggestion0(minMoneyPerTrade, symbol1, open1, current1, symbol2, open2, current2);
        } else {
            return computeBuySellSuggestion0(minMoneyPerTrade, symbol2, open2, current2, symbol1, open1, current1);
        }
    }

    /**
     * 卖出涨得好的，买入涨的差的
     */
    private EtfsCompareResp.Suggest computeBuySellSuggestion0(int minMoneyPerTrade,
                                                              String symbolLow, Double openLow, Double currentLow,
                                                              String symbolHigh, Double openHigh, Double currentHigh) {
        BigDecimal buyPoint = BigDecimal.valueOf(currentHigh)
            .divide(BigDecimal.valueOf(openHigh), 3, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(openLow));
        // 误差有0.001还能交易的话，更便于成交
        if (buyPoint.subtract(BigDecimal.valueOf(0.002)).doubleValue() > currentLow) {
            int quantityToSell = StockUtils.computeTradeQuantity(minMoneyPerTrade, currentHigh);
            BigDecimal moneyFromSell = BigDecimal.valueOf(quantityToSell).multiply(BigDecimal.valueOf(currentHigh));
            int exceptedQuantityToBuy = StockUtils.computeTradeQuantity(moneyFromSell.intValue(), buyPoint.doubleValue());


            int quantityToBuy = StockUtils.computeTradeQuantity(moneyFromSell.intValue(), currentLow);
            BigDecimal realMoneyFromBuy = BigDecimal.valueOf(currentLow).multiply(BigDecimal.valueOf(quantityToBuy));
            BigDecimal exceptedMoneyFromBuy = buyPoint.multiply(BigDecimal.valueOf(quantityToBuy));

            EtfsCompareResp.Suggest suggest = new EtfsCompareResp.Suggest();
            suggest.setCodeToSell(symbolHigh);
            suggest.setPriceToSell(currentHigh);
            suggest.setQuantityToSell(quantityToSell);
            suggest.setCodeToBuy(symbolLow);
            suggest.setExceptedPriceToBuy(buyPoint.doubleValue());
            suggest.setExceptedQuantityToBuy(exceptedQuantityToBuy);
            suggest.setPriceToBuy(currentLow);
            suggest.setQuantityToBuy(quantityToBuy);
            suggest.setExtraSavedMoney(exceptedMoneyFromBuy.subtract(realMoneyFromBuy).doubleValue());

            BigDecimal feeForBuy = StockTradeFeeUtils.computeFeeForBuy(
                BigDecimal.valueOf(suggest.getPriceToBuy()),
                BigDecimal.valueOf(suggest.getQuantityToBuy()),
                symbolLow,
                TradeFeeConfConstants.ETF_TRADE_FEE_CONF
            );
            BigDecimal feeForSell = StockTradeFeeUtils.computeFeeForSell(
                BigDecimal.valueOf(suggest.getPriceToSell()),
                BigDecimal.valueOf(quantityToSell),
                symbolHigh,
                TradeFeeConfConstants.ETF_TRADE_FEE_CONF
            );
            BigDecimal totalFee = feeForBuy.add(feeForSell);
            double netSaveMoney = BigDecimal.valueOf(suggest.getExtraSavedMoney())
                .subtract(totalFee).doubleValue();
            suggest.setTotalFee(totalFee.doubleValue());
            suggest.setNetSavedMoney(netSaveMoney);
            return suggest;
        }
        return null;
    }

}
