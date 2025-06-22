package cn.valuetodays.trade;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-23
 */
@Data
public class QmtOrderVo implements Serializable {
    private String order_id; // 订单编号，由qmt生成，可能为0（此时的订单不是经由qmt创建的）
    private String order_remark;
    private Integer order_status;
    private String order_status_str;
    private String order_sysid; // 合同编号，当天之中，由券商服务器生成，不会重复
    private Long order_time;
    private Integer order_type;
    private BigDecimal price; // 下单价格
    private BigDecimal traded_price; // 成交价格
    private Integer traded_volume;
    private String stock_code;
    private String strategy_name;
}
