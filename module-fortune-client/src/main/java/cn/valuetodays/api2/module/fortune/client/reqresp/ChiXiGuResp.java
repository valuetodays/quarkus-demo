package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-30
 */
@Data
public class ChiXiGuResp implements Serializable {
    private List<StockForGuXiResp> chiXiGuList;
    private BigDecimal rzRate;
    private BigDecimal rzMoney;
}
