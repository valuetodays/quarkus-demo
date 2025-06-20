package cn.valuetodays.api2.module.fortune.persist;

import java.math.BigDecimal;
import java.util.Objects;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * t0型etf每日数据
 *
 * @author lei.liu
 * @since 2025-01-04 11:44
 */
@Table(name = "fortune_etf_t0_daily_info")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class EtfT0DailyInfoPersist extends JpaCrudLongIdBasePersist {
    public static final BigDecimal DEFAULT_TRADE_VOLUME_WAN = BigDecimal.valueOf(-1);
    public static final BigDecimal DEFAULT_TOTAL_SHARES_WAN = BigDecimal.valueOf(-1);
    public static final BigDecimal DEFAULT_YI_JIA_PTG = BigDecimal.valueOf(-99);
    @Column(name = "stat_date")
    private Integer statDate;
    @Column(name = "code")
    private String code;
    @Column(name = "total_shares_wan")
    private BigDecimal totalSharesWan;
    @Column(name = "trade_volume_wan")
    private BigDecimal tradeVolumeWan;
    @Column(name = "trade_amount_wan")
    private BigDecimal tradeAmountWan;
    @Column(name = "huan_shou_ptg")
    private BigDecimal huanShouPtg;
    @Column(name = "yi_jia_ptg")
    private BigDecimal yiJiaPtg;
    @Column(name = "open_px")
    private BigDecimal openPx;
    @Column(name = "close_px")
    private BigDecimal closePx;
    @Column(name = "high_px")
    private BigDecimal highPx;
    @Column(name = "low_px")
    private BigDecimal lowPx;
    @Column(name = "inner_pan_wan")
    private BigDecimal innerPanWan;
    @Column(name = "outer_pan_wan")
    private BigDecimal outerPanWan;
    @Column(name = "liangbi")
    private BigDecimal liangbi;

    @JsonIgnore
    public void setTradeVolumeWanIfAbsent(BigDecimal tradeVolumeWanNew) {
        if (Objects.isNull(tradeVolumeWanNew) || DEFAULT_TRADE_VOLUME_WAN.compareTo(tradeVolumeWanNew) >= 0) {
            this.tradeVolumeWan = DEFAULT_TRADE_VOLUME_WAN;
        } else {
            this.tradeVolumeWan = tradeVolumeWanNew;
        }
    }

    @JsonIgnore
    public void setYiJiaPtgIfAbsent(BigDecimal yiJiaPtgNew) {
        if (Objects.isNull(yiJiaPtgNew) || DEFAULT_YI_JIA_PTG.compareTo(yiJiaPtgNew) >= 0) {
            this.yiJiaPtg = DEFAULT_YI_JIA_PTG;
        } else {
            this.yiJiaPtg = yiJiaPtgNew;
        }
    }

    public void setTotalSharesWanIfAbsent(BigDecimal tradeSharesWanNew) {
        if (Objects.isNull(tradeSharesWanNew) || DEFAULT_TOTAL_SHARES_WAN.compareTo(tradeSharesWanNew) >= 0) {
            this.totalSharesWan = DEFAULT_TOTAL_SHARES_WAN;
        } else {
            this.totalSharesWan = tradeSharesWanNew;
        }
    }
}
