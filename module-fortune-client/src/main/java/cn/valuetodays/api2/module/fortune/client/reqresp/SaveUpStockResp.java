package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 攒股-卖出策略.
 *
 * @author lei.liu
 * @since 2023-09-26
 */
@Data
public class SaveUpStockResp implements Serializable {
    private boolean computed;
    private SaveUpStockReq req;
    private BigDecimal totalAmountForBuy;
    private List<PossibleItem> possibleItemList;

    @Data
    public static class PossibleItem implements Serializable {
        private BigDecimal price;
        private int quantity;
        private BigDecimal amount;
        private BigDecimal cashEarned;
        private int saveUpQuantity;
        private BigDecimal marketValueForSaveUpQuantity;

    }
}
