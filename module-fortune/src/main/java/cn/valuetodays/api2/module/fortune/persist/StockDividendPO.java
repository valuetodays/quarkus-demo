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
 * 股票分红
 *
 * @author lei.liu
 * @since 2023-08-18
 */
@Table(name = "fortune_stock_dividend")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockDividendPO extends JpaCrudLongIdBasePersist {
    @Column(name = "code")
    private String code;
    @Column(name = "stat_date")
    private LocalDate statDate;
    @Column(name = "song_per_ten")
    private BigDecimal songPerTen;
    @Column(name = "zhuan_per_ten")
    private BigDecimal zhuanPerTen;
    @Column(name = "pai_per_ten")
    private BigDecimal paiPerTen;
}
