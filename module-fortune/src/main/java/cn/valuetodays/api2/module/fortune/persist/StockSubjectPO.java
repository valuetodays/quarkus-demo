package cn.valuetodays.api2.module.fortune.persist;

import cn.valuetodays.api2.module.fortune.enums.StockSubjectEnums;
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
 * 股票标的
 *
 * @author lei.liu
 * @since 2024-05-02 11:33
 */
@Table(name = "fortune_stock_subject")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockSubjectPO extends JpaCrudLongIdBasePersist {
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private StockSubjectEnums.Type type;

    @Column(name = "code")
    private String code;
}
