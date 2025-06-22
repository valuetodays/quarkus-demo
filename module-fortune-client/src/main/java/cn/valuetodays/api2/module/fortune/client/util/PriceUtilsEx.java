package cn.valuetodays.api2.module.fortune.client.util;

import java.math.BigDecimal;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.vt.rest.third.utils.StockCodeUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-23
 */
public class PriceUtilsEx {
    // 设置一个误差范围，例如 0.0000001
    private static final BigDecimal TOLERANCE = new BigDecimal("0.00000001");
    // 1厘
    private static final BigDecimal LI = BigDecimal.valueOf(0.001);


    private PriceUtilsEx() {

    }

    /**
     * 修正价格
     * 设置的交易价格是1.2，但在网络传输后python得到的可能变成1.19999999999或1.200000000000001
     *
     * @param price price
     */
    public static BigDecimal fixPrice(BigDecimal price) {
        //    # 保留三位小数：先×1000，后int，后÷1000
        double v = (int) (price.add(TOLERANCE).doubleValue() * 1000) / 1000.0;
        return BigDecimal.valueOf(v);
    }

    public static BigDecimal convertPriceDown(BigDecimal price, BigDecimal offset) {
        BigDecimal originPriceBd = fixPrice(price);
        return convertPriceDown0(originPriceBd, offset);
    }

    private static BigDecimal convertPriceDown0(BigDecimal price, BigDecimal offset) {
        return price.subtract(offset);
    }

    /**
     * 指定金额和价格，计算出股数
     */
    public static int computeQuantity(BigDecimal price, float money) {
        //    # 不管是否能整除尽，都额外添加100股吧
        int quantity = (int) (1.0 * money / price.doubleValue());
        return fixQuantity(quantity);
    }

    /**
     * 将数量处理成100的整数倍
     *
     * @param quantity 原数量，不一定是100的整数倍
     */
    public static int fixQuantity(int quantity) {
        return (quantity / 100) * 100 + 100;
    }

    public static boolean isValueConsideredEquals(BigDecimal v1, BigDecimal v2) {
        BigDecimal difference = v1.subtract(v2).abs();
        return difference.compareTo(TOLERANCE) <= 0;
    }

    /**
     * 计算一笔指定交易的对冲交易的价格
     *
     * @param trade 指定交易
     * @return 待对冲交易的价格
     */
    public static BigDecimal calcOppositePrice(StockTradePO trade) {
        final boolean buyFlag = trade.getTradeType().isBuyFlag();
        return trade.getPrice().add(
            BigDecimal.valueOf(buyFlag ? 1.0 : -1.0).multiply(
                calcOffsetForPrice(trade.getCode(), trade.getPrice(), trade.getQuantity())
            )
        );
    }

    private static BigDecimal calcOffsetForPrice(String code, BigDecimal price, int quantity) {
        if (StockCodeUtils.isStock(code)) {
            return BigDecimal.valueOf(0.1);
        }
        // etf
        // 1元以下 -> 0.001
        // 2元以下 -> 0.002
        // 3元以下 -> 0.003
        // 依次类推
        int n = (int) (price.doubleValue());

        BigDecimal offset = BigDecimal.valueOf((n + 1)).multiply(LI);
        BigDecimal expectedEarned = offset.multiply(BigDecimal.valueOf(quantity));
        while (expectedEarned.compareTo(BigDecimal.valueOf(0.4)) < 0) {
            offset = offset.add(LI);
            expectedEarned = offset.multiply(BigDecimal.valueOf(quantity));
        }
        return offset;
    }
}
