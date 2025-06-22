package cn.valuetodays.api2.module.fortune.client.persist;

import cn.valuetodays.api2.module.fortune.client.enums.FundTradeMonthlyStatEnums;
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
@Table(name = "fortune_fund_trade_monthly_stat")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class FundTradeMonthlyStatPO extends JpaCrudLongIdBasePersist {

    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private FundTradeMonthlyStatEnums.Channel channel;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private FundTradeMonthlyStatEnums.Type type;
    // yyyyMM
    @Column(name = "stat_month")
    private Integer statMonth;
    @Column(name = "gua_pai_number")
    private Integer guaPaiNumber;
    @Column(name = "busi_amount")
    private Long busiAmount;
    @Column(name = "busi_money")
    private Long busiMoney;

}
