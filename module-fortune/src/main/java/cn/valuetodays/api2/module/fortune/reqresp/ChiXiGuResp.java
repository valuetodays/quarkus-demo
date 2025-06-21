package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.component.reqresp.StockForGuXiResp;
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
