package cn.vt.rest.third.utils;

import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import cn.vt.rest.third.StockEnums;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.liu
 * @since 2021-04-25 11:23
 */
@Slf4j
public final class StockUtils {

    private static final Map<StockEnums.Region, String> REGION_AND_CODE_MAP;

    static {
        Map<StockEnums.Region, String> map = new EnumMap<>(StockEnums.Region.class);
        map.put(StockEnums.Region.SHANGHAI, "SS");
        map.put(StockEnums.Region.SHENZHEN, "SZ");
        REGION_AND_CODE_MAP = Map.copyOf(map);
    }

    private StockUtils() {
    }

    /**
     * 计算交易股数.
     * 注意：因为股数必须是100的整数倍，所以得到的股数*price会稍微大于money
     *
     * @param money 金额
     * @param price 单价
     */
    public static int computeTradeQuantity(int money, double price) {
        return computeTradeQuantity(money, price, false);
    }

    /**
     * 计算交易股数.
     * 注意：因为股数必须是100的整数倍.
     *
     * @param money      金额
     * @param price      单价
     * @param asMaxMoney 是否作为最大金额。若为true，则股数*price一定会小于money；否则会稍微大于money
     */
    public static int computeTradeQuantity(int money, double price, boolean asMaxMoney) {
        double numberDouble = 1.0 * money / price;
        int numberInt = (int) numberDouble;
        // 获取100的整数倍 （将原数+50再除以100，去掉最后两位，再乘以100，即是结果）
        int numberIntWith100x = ((numberInt + 50) / 100) * 100;
        while (numberIntWith100x * price < money) {
            numberIntWith100x += 100;
        }
        if (asMaxMoney) {
            while (numberIntWith100x * price > money) {
                numberIntWith100x -= 100;
            }
        }
        return numberIntWith100x;
    }

    public static String getRegion(StockEnums.Region region) {
        return REGION_AND_CODE_MAP.get(region);
    }

    public static String prependRegion(String code) {
        StockEnums.Region region = StockEnums.Region.byCode(code);
        String codeToUse = code;
        if (Objects.nonNull(region)) {
            codeToUse = region.getShortCode() + code;
        }
        return codeToUse;
    }

    public static boolean isInTradeTime(LocalTime time) {
        return (time.isAfter(LocalTime.of(9, 30)) && time.isBefore(LocalTime.of(11, 30)))
            || (time.isAfter(LocalTime.of(13, 0)) && time.isBefore(LocalTime.of(15, 0)));
    }
}
