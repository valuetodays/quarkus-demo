package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradeMonitorPO;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-10
 */
@Data
public class TradeMonitorPriceResp implements Serializable {
    private StockTradeMonitorPO monitor;
    private String name;
    private Double realtimePrice;
    private String suggest;
    private String xueqiuStockUrl;
    private String orderStatusStr;

    @JsonIgnore
    public void computeSuggest(FortuneCommonEnums.TradeType tradeType, BigDecimal price) {
        if (Objects.isNull(realtimePrice)) {
            return;
        }
        if (FortuneCommonEnums.TradeType.BUY == tradeType) {
            // 当前价 小于 设定价，就可以买
            if (BigDecimal.valueOf(realtimePrice).compareTo(price) <= 0) {
                this.setSuggest("可买");
            }
        } else if (FortuneCommonEnums.TradeType.SELL == tradeType) {
            // 当前价 大于 设定价，就可以卖
            if (BigDecimal.valueOf(realtimePrice).compareTo(price) >= 0) {
                this.setSuggest("可卖");
            }
        }
    }

}
