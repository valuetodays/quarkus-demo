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
@Table(name = "fortune_etf_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class EtfInfoPO extends JpaCrudLongIdBasePersist {
    @Column(name = "code")
    private String code;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "release_date")
    private String releaseDate;
    @Column(name = "scale")
    private String scale;
    @Column(name = "huan_shou_ptg")
    private BigDecimal huanShouPtg;
    @Column(name = "total_shares")
    private BigDecimal totalShares;
    @Column(name = "fenhong")
    private String fenhong;
    @Column(name = "manage_radio")
    private String manageRadio;
    @Column(name = "holder_radio")
    private String holderRadio;
    @Column(name = "sell_radio")
    private String sellRadio;
    @Column(name = "busi_compare_base")
    private String busiCompareBase;
    @Column(name = "follow_index")
    private String followIndex;
    @Column(name = "type")
    private String type;

}
