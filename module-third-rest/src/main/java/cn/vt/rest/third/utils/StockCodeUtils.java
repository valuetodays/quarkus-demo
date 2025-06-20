package cn.vt.rest.third.utils;

import java.util.Objects;

import cn.vt.rest.third.StockEnums;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-04
 */
public class StockCodeUtils {
    private StockCodeUtils() {
    }

    /**
     * 构建雪球平台使用的股票编号. 600036 -> SH600036
     *
     * @param stockCode 600036
     * @return SH600036
     */
    public static String buildForXueQiu(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return stockCode;
        }
        if (StringUtils.startsWithAny(stockCode, StockEnums.REGION_SHORT_CODE_LIST)) {
            return stockCode;
        }
        StockEnums.Region region = StockEnums.Region.byCode(stockCode);
        if (Objects.isNull(region)) {
            return stockCode;
        }
        String prefix = region.getShortCode();
        if (stockCode.startsWith(prefix)) {
            return stockCode;
        }
        return prefix + stockCode;
    }

    /**
     * e海方舟-量化交易版
     *
     * @param stockCode 600036
     * @return 600036.SH
     */
    public static String buildForEhaifangzhou(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return stockCode;
        }
        StockEnums.Region region = StockEnums.Region.byCode(stockCode);
        if (Objects.isNull(region)) {
            return stockCode;
        }
        String suffix = "." + region.getRegionShortCode();
        if (stockCode.endsWith(suffix)) {
            return stockCode;
        }
        return stockCode + suffix;
    }

    /**
     * e海方舟-量化交易版
     *
     * @param stockCode 600036.SH
     * @return 600036
     */
    public static String parseFromEhaifangzhou(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return stockCode;
        }
        if (stockCode.length() != "600036.SH".length()) {
            return stockCode;
        }
        return stockCode.substring(0, "600036".length());
    }


    /**
     * @param codeWithRegion SH600036
     * @return 600036
     */
    public static String parseCode(String codeWithRegion) {
        if (StringUtils.isBlank(codeWithRegion)) {
            return codeWithRegion;
        }
        if (!StringUtils.startsWithAny(codeWithRegion, StockEnums.REGION_SHORT_CODE_LIST)) {
            return codeWithRegion;
        }
        String tempCode = codeWithRegion;
        for (String s : StockEnums.REGION_SHORT_CODE_LIST) {
            tempCode = StringUtils.replace(tempCode, s, StringUtils.EMPTY);
        }
        return tempCode;
    }


    /**
     * 把{code}.{market}修改成{market}{code}
     * 因为播放语音时600036.SH会识别成数字，而SH600036就不会
     *
     * @param codeWithRegion
     * @return
     */
    public static String convert_code_for_market_code(String codeWithRegion) {
        if (StringUtils.isBlank(codeWithRegion)) {
            return codeWithRegion;
        }
        if (codeWithRegion.length() == "600036.SH".length()) {
            return StringUtils.substringAfter(codeWithRegion, ".")
                + StringUtils.substringBefore(codeWithRegion, ".");
        }

        return codeWithRegion;

    }


    public static boolean isStockInShangHai(String stockCode) {
        return StringUtils.startsWithAny(stockCode, "6");
    }

    public static boolean isStockInShenZhen(String stockCode) {
        return StringUtils.startsWithAny(stockCode, "0", "3");
    }

    public static boolean isStock(String code) {
        return isStockInShangHai(code) || isStockInShenZhen(code);
    }

}
