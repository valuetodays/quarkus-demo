package cn.valuetodays.api2.module.fortune.persist;

import java.util.Date;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import cn.vt.rest.third.eastmoney.QuoteEnums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 指数信息
 *
 * @author lei.liu
 * @since 2023-04-02 13:12
 */
@Table(name = "fortune_quote")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class QuotePO extends JpaCrudLongIdBasePersist {

    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "region")
    @Enumerated(EnumType.STRING)
    private QuoteEnums.Region region;
    @Column(name = "release_date")
    private Date releaseDate;
    @Column(name = "data_base_date")
    private Date dataBaseDate;
    @Column(name = "suggest_etfs")
    private String suggestEtfs;

}
