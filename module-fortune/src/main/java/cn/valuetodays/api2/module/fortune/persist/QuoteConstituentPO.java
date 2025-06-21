package cn.valuetodays.api2.module.fortune.persist;

import java.time.LocalDate;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 指数成份股
 *
 * @author lei.liu
 * @since 2023-08-16
 */
@Table(name = "fortune_quote_constituent")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class QuoteConstituentPO extends JpaCrudLongIdBasePersist {

    @Column(name = "quote_id")
    private Long quoteId;
    @Column(name = "stat_date")
    private LocalDate statDate;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;

}
