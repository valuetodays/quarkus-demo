package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-01
 */
@Data
public class TradeByMonitorIdReq implements Serializable {
    @NotNull
    private Long monitorId;
    @NotNull
    private BigDecimal realtimePrice;
}
