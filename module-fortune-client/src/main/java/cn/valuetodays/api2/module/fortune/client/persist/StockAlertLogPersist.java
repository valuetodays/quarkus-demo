package cn.valuetodays.api2.module.fortune.client.persist;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 股票告警记录表
 *
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@Table(name = "fortune_stock_alert_log")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true /*, value = {"hibernateLazyInitializer", "handler"}*/)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockAlertLogPersist extends JpaCrudLongIdBasePersist {

    @Column(name = "alert_id")
    private Long alertId;
    @Column(name = "config_point")
    private BigDecimal configPoint;
    @Column(name = "target_point")
    private BigDecimal targetPoint;
    @Column(name = "target_ptg")
    private BigDecimal targetPtg;

    @Transient
    private Map<String, Object> otherMap = new HashMap<>();
}
