package cn.valuetodays.api2.module.fortune.component.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-31
 */
@Data
public class StockForGuXiResp implements Serializable {
    private String code;
    private String name;
    // 融资金额
    private int rzMoney;
    private Double price;
    private int quantity;
    private Double realUseMoney;
    private Double guziRate;
    private Double last10Pai;
    private Double nextFenhongMoney;
    // 融资利息
    private Double rzInterest;
    // 是否划算
    private boolean bigDeal;
}
