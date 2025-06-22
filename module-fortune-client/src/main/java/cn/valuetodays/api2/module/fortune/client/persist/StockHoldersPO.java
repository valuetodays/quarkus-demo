package cn.valuetodays.api2.module.fortune.client.persist;

import java.math.BigDecimal;

import cn.valuetodays.quarkus.commons.base.jpa.JpaAccountableLongIdEntity;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 股票持仓
 *
 * @author lei.liu
 * @since 2023-01-10 21:38
 */
@Table(name = "fortune_stock_holders")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockHoldersPO extends JpaAccountableLongIdEntity {
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private FortuneCommonEnums.Channel channel;
    @Column(name = "stat_date")
    private Integer statDate;
    @Column(name = "sec_code")
    private String secCode;
    @Column(name = "hold_volume")
    private Integer holdVolume;
    @Column(name = "cost_price")
    private BigDecimal costPrice;
    @Column(name = "market_price")
    private BigDecimal marketPrice;
}
