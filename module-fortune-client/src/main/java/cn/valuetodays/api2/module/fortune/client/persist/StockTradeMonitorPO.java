package cn.valuetodays.api2.module.fortune.client.persist;

import java.math.BigDecimal;

import cn.valuetodays.api2.module.fortune.client.enums.StockTradeMonitorEnums;
import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-10
 */
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "fortune_stock_trade_monitor")
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.Data
public class StockTradeMonitorPO extends JpaCrudLongIdBasePersist {

    @Column(name = "code")
    private String code;
    @Column(name = "trade_type")
    @Enumerated(EnumType.STRING)
    private FortuneCommonEnums.TradeType tradeType;
    @Column(name = "quantity")
    @Schema(description = "成交数量")
    private int quantity;
    @Column(name = "price")
    @Schema(description = "成交单价")
    private BigDecimal price;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StockTradeMonitorEnums.Status status;
    @Column(name = "trade_id")
    private Long tradeId;
    @Column(name = "remark")
    private String remark;
    @Column(name = "trade_sysid")
    private String tradeSysid;
}
