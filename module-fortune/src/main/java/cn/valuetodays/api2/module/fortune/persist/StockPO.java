package cn.valuetodays.api2.module.fortune.persist;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import cn.vt.rest.third.StockEnums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-01-27 11:43
 */
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "fortune_stock")
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.Data
public class StockPO extends JpaCrudLongIdBasePersist {
    // 需要添加字段标识是股票/etf/lof
    @Column(name = "code")
    private String code;
    @Column(name = "region")
    @Enumerated(EnumType.STRING)
    private StockEnums.Region region;
    @Column(name = "name")
    private String name;
    @Column(name = "remark")
    private String remark;

    public static StockPO of(String name, StockEnums.Region region, String code) {
        StockPO stockPO = new StockPO();
        stockPO.setName(name);
        stockPO.setRegion(region);
        stockPO.setCode(code);
        return stockPO;
    }
}
