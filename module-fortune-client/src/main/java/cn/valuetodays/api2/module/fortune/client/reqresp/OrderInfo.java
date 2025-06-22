package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 通用订单（委托）.
 * 和券商无关
 *
 * @author lei.liu
 * @since 2024-05-22
 */
@Data
public class OrderInfo implements Serializable {
    private String order_id; // 订单编号在海通服务端重启后会变成空。
    private String order_remark;
    private Integer order_status;
    private String order_status_str;
    private String order_sysid;
    private Long order_time;
    private Integer order_type;
    private Integer traded_volume;
    private BigDecimal traded_price;
    private String stock_code; // 600036.SH
    private String strategy_name;

}
