package cn.valuetodays.api2.module.fortune.persist;

import cn.valuetodays.api2.module.fortune.enums.QuoteDailyStatEnums;
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
 * 指数每日统计
 *
 * @author lei.liu
 * @since 2023-04-02 13:02
 */
@Table(name = QuoteDailyStatPO.TABLE_NAME)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class QuoteDailyStatPO extends JpaCrudLongIdBasePersist {

    public static final String TABLE_NAME = "fortune_quote_daily_stat";

    @Column(name = "stat_date")
    private Integer statDate;
    @Column(name = "code")
    private String code;
    @Column(name = "region")
    private String region;
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private QuoteDailyStatEnums.Channel channel;
    @Column(name = "close_px")
    private Double closePx;
    @Column(name = "high_px")
    private Double highPx;
    @Column(name = "low_px")
    private Double lowPx;
    @Column(name = "open_px")
    private Double openPx;
    @Column(name = "busi_amount")
    private Long busiAmount;
    @Column(name = "busi_money")
    private Long busiMoney;
    @Column(name = "offset_px")
    private Double offsetPx;
    @Column(name = "offset_px_percentage")
    private Double offsetPxPercentage;
    @Column(name = "zhenfu_percentage")
    private Double zhenfuPercentage;
    @Column(name = "huanshoulv_percentage")
    private Double huanshoulvPercentage;

}
