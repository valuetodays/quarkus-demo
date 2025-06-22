package cn.valuetodays.api2.module.fortune.client.persist;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 股票对买对卖交易
 *
 * @author lei.liu
 * @since 2024-04-24 20:34
 */
@Table(name = "fortune_stock_pair_trade")
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class StockPairTradePO extends JpaCrudLongIdBasePersist {


    @Column(name = "trade_id1")
    private Long tradeId1;
    @Column(name = "trade_id2")
    private Long tradeId2;
    @Column(name = "trade_id3")
    private Long tradeId3;
    @Column(name = "remark")
    private String remark;
    @Column(name = "create_user_id")
    private Long createUserId;
    @Column(name = "update_user_id")
    private Long updateUserId;
}
