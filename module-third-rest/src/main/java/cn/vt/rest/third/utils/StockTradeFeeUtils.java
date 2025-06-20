package cn.vt.rest.third.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cn.vt.exception.AssertUtils;
import cn.vt.exception.CommonException;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;

/**
 * 计算股票交易手续费.
 *
 * @author lei.liu
 * @since 2023-05-10 11:26
 */
public final class StockTradeFeeUtils {
    private StockTradeFeeUtils() {
        // private
    }

    public static BigDecimal computeYongJinFee(BigDecimal price, BigDecimal quantity,
                                               ITradeFeeConf tradeFeeConf) {
        BigDecimal amount = price.multiply(quantity);
        BigDecimal yjFee = amount.multiply(tradeFeeConf.yongjinRate()).setScale(2, RoundingMode.HALF_UP);
        return yjFee.max(tradeFeeConf.leastYongjinAmount());
    }

    public static BigDecimal computeLeastSellPrice(BigDecimal stockBuyPrice, BigDecimal stockNum,
                                                   BigDecimal feeForBuy,
                                                   String stockCode,
                                                   ITradeFeeConf tradeFeeConf) {
        BigDecimal tradeAmount = stockBuyPrice.multiply(stockNum);
        BigDecimal totalAmountWithBuyFee = tradeAmount.add(feeForBuy);
        BigDecimal sellPrice = totalAmountWithBuyFee.divide(stockNum, RoundingMode.HALF_UP);
        BigDecimal sellFee = computeFeeForSell(sellPrice, stockNum, stockCode, tradeFeeConf);
        BigDecimal totalAmountWithBuyFeeAndSellFee = totalAmountWithBuyFee.add(sellFee);
        sellPrice = totalAmountWithBuyFeeAndSellFee.divide(stockNum, RoundingMode.HALF_UP);
        return sellPrice;
    }

    public static BigDecimal computeFeeForSell(BigDecimal sellPrice, BigDecimal stockNum,
                                               String stockCode, ITradeFeeConf tradeFeeConf) {
        AssertUtils.assertNotNull(tradeFeeConf);
        BigDecimal tradeAmount = sellPrice.multiply(stockNum);
        // 佣金
        BigDecimal yongjinAmount = tradeAmount.multiply(tradeFeeConf.yongjinRate()).setScale(2, RoundingMode.HALF_UP);
        yongjinAmount = yongjinAmount.max(tradeFeeConf.leastYongjinAmount());
        // 过户费
        BigDecimal guohuAmount = computeGuohuFee(tradeAmount, stockCode, tradeFeeConf);
        // 印花费
        BigDecimal yinhuaAmount = computeYinhuaFee(tradeAmount, FortuneCommonEnums.TradeType.SELL, tradeFeeConf);
        return yongjinAmount.add(guohuAmount).add(yinhuaAmount);
    }

    public static BigDecimal computeFeeForBuy(BigDecimal stockBuyPrice, BigDecimal stockQuantity,
                                              String stockCode, ITradeFeeConf tradeFeeConf) {
        AssertUtils.assertNotNull(tradeFeeConf);
        BigDecimal tradeAmount = stockBuyPrice.multiply(stockQuantity);
        // 佣金
        BigDecimal yongjinAmount = tradeAmount.multiply(tradeFeeConf.yongjinRate()).setScale(2, RoundingMode.HALF_UP);
        yongjinAmount = yongjinAmount.max(tradeFeeConf.leastYongjinAmount());
        // 过户费
        BigDecimal guohuAmount = computeGuohuFee(tradeAmount, stockCode, tradeFeeConf);
        // 印花费
        BigDecimal yinhuaAmount = computeYinhuaFee(tradeAmount, FortuneCommonEnums.TradeType.BUY, tradeFeeConf);

        return yongjinAmount.add(guohuAmount).add(yinhuaAmount);
    }

    public static BigDecimal computeGuohuFee(BigDecimal price,
                                             BigDecimal quantity,
                                             String stockCode,
                                             ITradeFeeConf tradeFeeConf) {
        BigDecimal tradeAmount = price.multiply(quantity);
        return computeGuohuFee(tradeAmount, stockCode, tradeFeeConf);
    }

    public static BigDecimal computeGuohuFee(BigDecimal tradeAmount,
                                             String stockCode,
                                             ITradeFeeConf tradeFeeConf) {
        return tradeAmount.multiply(tradeFeeConf.guohuRate(stockCode)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal computeYinhuaFee(BigDecimal price,
                                              BigDecimal quantity,
                                              FortuneCommonEnums.TradeType tradeType,
                                              ITradeFeeConf tradeFeeConf) {
        BigDecimal tradeAmount = price.multiply(quantity);
        return computeYinhuaFee(tradeAmount, tradeType, tradeFeeConf);
    }

    public static BigDecimal computeYinhuaFee(BigDecimal tradeAmount,
                                              FortuneCommonEnums.TradeType tradeType,
                                              ITradeFeeConf tradeFeeConf) {
        AssertUtils.assertNotNull(tradeFeeConf);

        if (FortuneCommonEnums.TradeType.BUY == tradeType) {
            return tradeAmount.multiply(tradeFeeConf.yinhuaRateForBuy()).setScale(2, RoundingMode.HALF_UP);
        } else if (FortuneCommonEnums.TradeType.SELL == tradeType) {
            return tradeAmount.multiply(tradeFeeConf.yinhuaRateForSell()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new CommonException("unknown tradeType: " + tradeType);
    }
}
