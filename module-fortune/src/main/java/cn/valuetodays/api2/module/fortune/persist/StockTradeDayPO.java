package cn.valuetodays.api2.module.fortune.persist;

import java.util.List;

import cn.valuetodays.api2.client.bases.jpa.ListIntegerJsonAttributeConverter;
import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import cn.vt.rest.third.StockEnums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 股票交易日
 *
 * @author lei.liu
 * @since 2024-05-01 02:02
 */
@Table(name = "fortune_stock_trade_day")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockTradeDayPO extends JpaCrudLongIdBasePersist {

    @Column(name = "year")
    private Integer year;
    @Column(name = "region")
    @Enumerated(EnumType.STRING)
    private StockEnums.Region region;
    @Column(name = "content")
    @Convert(converter = ListIntegerJsonAttributeConverter.class)
    private List<Integer> content;
}
