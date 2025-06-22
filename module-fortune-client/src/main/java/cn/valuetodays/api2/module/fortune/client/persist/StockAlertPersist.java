package cn.valuetodays.api2.module.fortune.client.persist;

import java.math.BigDecimal;

import cn.valuetodays.api2.module.fortune.client.enums.StockAlertEnums;
import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 股票告警
 *
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@Table(name = "fortune_stock_alert")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true /*, value = {"hibernateLazyInitializer", "handler"}*/)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockAlertPersist extends JpaCrudLongIdBasePersist {

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StockAlertEnums.Status status;
    @Column(name = "title")
    private String title;
    @Column(name = "code")
    private String code;
    @Column(name = "code_type")
    @Enumerated(EnumType.STRING)
    private StockAlertEnums.CodeType codeType;
    @Column(name = "schedule_type")
    @Enumerated(EnumType.STRING)
    private StockAlertEnums.ScheduleType scheduleType;
    @Column(name = "current_point")
    private BigDecimal currentPoint;
    @Column(name = "target_point")
    private BigDecimal targetPoint;
    @Column(name = "target_ptg")
    private BigDecimal targetPtg;
}
