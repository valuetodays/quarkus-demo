package cn.valuetodays.api2.module.fortune.persist;

import java.math.BigDecimal;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行业每日统计
 *
 * @author lei.liu
 * @since 2025-05-17 23:53
 */
@Table(name = "fortune_industry_daily_stat")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true /*, value = {"hibernateLazyInitializer", "handler"}*/)
@EqualsAndHashCode(callSuper = true)
@Data
public class IndustryDailyStatPersist extends JpaCrudLongIdBasePersist {

    @Column(name = "stat_date")
    private Integer statDate;
    @Column(name = "code")
    private String code;
    @Column(name = "title")
    private String title;
    @Column(name = "close_px")
    private BigDecimal closePx;
    @Column(name = "chg_ptg")
    private BigDecimal chgPtg;
    @Column(name = "chg_value")
    private BigDecimal chgValue;
    @Column(name = "huan_shou_lv_ptg")
    private BigDecimal huanShouLvPtg;
    @Column(name = "total_cap_yi")
    private BigDecimal totalCapYi;
    @Column(name = "zhang_jia_shu")
    private Integer zhangJiaShu;
    @Column(name = "die_jia_shu")
    private Integer dieJiaShu;
    @Column(name = "ling_zhang_stock")
    private String lingZhangStock;
    @Column(name = "ling_zhang_chg_ptg")
    private BigDecimal lingZhangChgPtg;
    @Column(name = "ling_die_stock")
    private String lingDieStock;
    @Column(name = "ling_die_chg_ptg")
    private BigDecimal lingDieChgPtg;
    @Column(name = "remark")
    private String remark;
}
