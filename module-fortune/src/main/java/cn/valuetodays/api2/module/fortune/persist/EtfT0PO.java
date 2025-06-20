package cn.valuetodays.api2.module.fortune.persist;

import java.math.BigDecimal;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "fortune_etf_t0")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class EtfT0PO extends JpaCrudLongIdBasePersist {
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "release_date")
    private Integer releaseDate;
    @Column(name = "total_shares")
    private BigDecimal totalShares;
    @Column(name = "trade_amount")
    private BigDecimal tradeAmount;
    @Column(name = "huan_shou_ptg")
    private BigDecimal huanShouPtg;
    @Column(name = "yi_jia_ptg")
    private BigDecimal yiJiaPtg;
    @Column(name = "manage_radio")
    private BigDecimal manageRadio;
    @Column(name = "holder_radio")
    private BigDecimal holderRadio;
    @Column(name = "follow_index")
    private String followIndex;

}
