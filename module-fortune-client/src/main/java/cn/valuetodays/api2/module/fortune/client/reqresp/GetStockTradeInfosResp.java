package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-03-26
 */
@Data
public class GetStockTradeInfosResp implements Serializable {
    private List<StockTradePO> tradeList;

    public static GetStockTradeInfosResp empty() {
        return new GetStockTradeInfosResp();
    }
}
