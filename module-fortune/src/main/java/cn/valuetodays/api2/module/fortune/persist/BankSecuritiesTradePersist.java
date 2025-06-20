package cn.valuetodays.api2.module.fortune.persist;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cn.valuetodays.api2.module.fortune.enums.BankSecuritiesTradeEnums;
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
 * 银证交易
 *
 * @author lei.liu
 * @since 2024-08-19 17:32
 */
@Table(name = "fortune_bank_securities_trade")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class BankSecuritiesTradePersist extends JpaAccountableLongIdEntity {

    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private FortuneCommonEnums.Channel channel;
    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    private BankSecuritiesTradeEnums.Direction direction;
    @Column(name = "operate_time")
    private LocalDateTime operateTime;
    @Column(name = "money")
    private BigDecimal money;
}
