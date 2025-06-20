package cn.vt.rest.third.utils;

import java.math.BigDecimal;

import cn.vt.exception.CommonException;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-24 11:27
 */
public final class TradeFeeConfConstants {
    public static final ITradeFeeConf ETF_TRADE_FEE_CONF = new ITradeFeeConf() {
        @Override
        public Type type() {
            return Type.ETF;
        }

        @Override
        public BigDecimal yongjinRate() {
            return BigDecimal.valueOf(0.5 / 10000.0);
        }

        @Override
        public BigDecimal leastYongjinAmount() {
            return BigDecimal.valueOf(0.1);
        }

        @Override
        public BigDecimal guohuRate(String stockCode) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal yinhuaRateForBuy() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal yinhuaRateForSell() {
            return BigDecimal.ZERO;
        }
    };
    public static final ITradeFeeConf STOCK_TRADE_FEE_CONF = new ITradeFeeConf() {
        @Override
        public Type type() {
            return Type.STOCK;
        }

        @Override
        public BigDecimal yongjinRate() {
            // 万0.864
            return BigDecimal.valueOf(0.864 / 10000.0);
        }

        @Override
        public BigDecimal leastYongjinAmount() {
            // 最低0.1
            return BigDecimal.valueOf(0.1);
        }

        @Override
        public BigDecimal guohuRate(String stockCode) {
            if (StockCodeUtils.isStockInShenZhen(stockCode)) {
                return BigDecimal.ZERO;
            } else if (StockCodeUtils.isStockInShangHai(stockCode)) {
                return BigDecimal.valueOf(0.1 / 10000.0);
            }
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal yinhuaRateForBuy() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal yinhuaRateForSell() {
            // 万5
            return BigDecimal.valueOf(5.0 / 10000.0);
        }
    };

    private TradeFeeConfConstants() {
    }

    public static ITradeFeeConf getTradeFeeConfByCode(String code) {
        if (StringUtils.startsWithAny(code, "5", "1")) {
            return ETF_TRADE_FEE_CONF;
        } else if (StringUtils.startsWithAny(code, "6", "0", "3")) {
            return STOCK_TRADE_FEE_CONF;
        }
        throw new CommonException("illegal code: " + code + ", should start with 5/1/6/0/3");
    }

}
