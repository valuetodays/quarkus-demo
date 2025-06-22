package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 持仓.
 *
 * @author lei.liu
 * @since 2023-05-29 09:21
 */
@Data
public class HolderInfo implements Serializable {
    private String code;
    private String name;
    private int stockQuantity;
    private int stockAvailableQuantity;
    private BigDecimal costPrice;
    private BigDecimal marketPrice;
    private String shareAccount;
}
