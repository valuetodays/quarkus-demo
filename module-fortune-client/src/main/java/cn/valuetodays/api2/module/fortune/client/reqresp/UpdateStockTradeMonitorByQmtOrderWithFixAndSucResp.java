package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-02-21
 */
@Data
public class UpdateStockTradeMonitorByQmtOrderWithFixAndSucResp implements Serializable {
    private int totalOrders;
    private int updatedStockTradeMonitor;
}
