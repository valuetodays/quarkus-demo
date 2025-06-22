package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-21
 */
@Data
public class QmtAnalysisOrdersResp implements Serializable {
    private String error;
    private int succeedOrders;
    private int matchedOrders;

    private BigDecimal amount_pair_total_decimal;
    private int count_orders;
    private BigDecimal money_occupy_total;
    private int order_occupy_total;

    private BigDecimal amount_pair_auto_decimal;
    private int count_orders_auto;
    private BigDecimal money_occupy_auto_pair;
    private int order_occupy_auto_pair;

    public static QmtAnalysisOrdersResp ofError(String errorMsg) {
        QmtAnalysisOrdersResp resp = new QmtAnalysisOrdersResp();
        resp.setError(errorMsg);
        return resp;
    }

    @JsonIgnore
    public String buildResultMsg() {
        String error = this.getError();
        BigDecimal amount_pair_total_decimal = this.getAmount_pair_total_decimal();
        BigDecimal money_occupy_total = this.getMoney_occupy_total();
        int order_occupy_total = this.getOrder_occupy_total();
        BigDecimal amount_pair_auto_decimal = this.getAmount_pair_auto_decimal();
        BigDecimal money_occupy_auto_pair = this.getMoney_occupy_auto_pair();
        int order_occupy_auto_pair = this.getOrder_occupy_auto_pair();
        if (StringUtils.isNotBlank(error)) {
            return error;
        }

        String s = "total pairs earned " + (amount_pair_total_decimal)
            + " with " + (count_orders) + " orders"
            + ", but occupy " + (money_occupy_total)
            + " with " + (order_occupy_total) + " orders. ";
        s += "\nauto pairs earned " + (amount_pair_auto_decimal)
            + " with " + (count_orders_auto) + " orders"
            + ", but occupy " + (money_occupy_auto_pair)
            + " with " + (order_occupy_auto_pair) + " orders ";
        return "\n" + s;
    }

}
