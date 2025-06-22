package cn.valuetodays.api2.module.fortune.client.persist;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * cpi数据
 *
 * @author lei.liu
 * @since 2023-01-13 16:58
 */
@Table(name = "fortune_cpi")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CpiPO extends JpaCrudLongIdBasePersist {
    @Column(name = "stat_year_month")
    private Integer statYearMonth;
    @Column(name = "national_accumulate")
    private Double nationalAccumulate;
    @Column(name = "national_base")
    private Double nationalBase;
    @Column(name = "city_accumulate")
    private Double cityAccumulate;
    @Column(name = "city_base")
    private Double cityBase;
    @Column(name = "rural_accumulate")
    private Double ruralAccumulate;
    @Column(name = "rural_base")
    private Double ruralBase;
    @Column(name = "remark")
    private String remark;
}
