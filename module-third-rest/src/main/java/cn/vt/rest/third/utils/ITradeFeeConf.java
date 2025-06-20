package cn.vt.rest.third.utils;

import java.math.BigDecimal;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-23 11:28
 */
public interface ITradeFeeConf {
    Type type();

    // 佣金 万2.5
    BigDecimal yongjinRate();

    // 最低佣金 5元
    BigDecimal leastYongjinAmount();

    // 过户费 万0.1, 深市不收（股票编号0/3开头）
    BigDecimal guohuRate(String stockCode);

    // 印花税 万10
    BigDecimal yinhuaRateForBuy();

    BigDecimal yinhuaRateForSell();

    enum Type {
        STOCK,
        ETF
    }
}
