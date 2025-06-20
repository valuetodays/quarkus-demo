package cn.valuetodays.api2.module.fortune.persist;

import java.math.BigDecimal;
import java.time.LocalDate;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * etf的iopv
 *
 * @author lei.liu
 * @since 2024-09-16 21:42
 */
@Table(name = "fortune_etf_iopv")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class EtfIopvPO extends JpaCrudLongIdBasePersist {
    @Column(name = "code")
    private String code;
    @Column(name = "stat_date")
    private LocalDate statDate;
    @Column(name = "iopv")
    private BigDecimal iopv;
}
