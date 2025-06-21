package cn.valuetodays.api2.module.fortune.component.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-16
 */
@Data
public class StockForGuXiReq implements Serializable {
    private String code;
    private String name;
    private double last10Pai;

    public StockForGuXiReq() {
    }

    public StockForGuXiReq(String code, String name, double last10Pai) {
        this.code = code;
        this.name = name;
        this.last10Pai = last10Pai;
    }
}
