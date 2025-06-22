package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-24 11:00
 */
@Data
public class EtfsCompareReq implements Serializable {
    // 默认-每次交易的最小金额
    public static final int DEFAULT_MIN_MONEY_PER_TRADE = 2000 * 5;
    private String group;
    private int tn;
    private List<String> etfs;
    // 每次交易的最小金额
    private int minMoneyPerTrade = DEFAULT_MIN_MONEY_PER_TRADE;
}
