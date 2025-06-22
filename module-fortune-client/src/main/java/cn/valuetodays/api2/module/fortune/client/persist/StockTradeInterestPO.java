package cn.valuetodays.api2.module.fortune.client.persist;

import java.math.BigDecimal;
import java.time.LocalTime;

import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import jakarta.persistence.Column;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 融资交易的利息.
 * <p>
 * 数据来源：华宝证券，查询 -> 资金流水
 *
 * @author lei.liu
 * @since 2023-10-17
 */
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "fortune_stock_trade_interest")
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.Data
@Schema(name = "股票交易-利息")
public class StockTradeInterestPO extends JpaCrudLongIdBasePersist {
    // todo add account_id
    @Column(name = "trade_date")
    private Integer tradeDate;
    @Column(name = "trade_time")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime tradeTime;
    @Column(name = "amount_fee")
    @Schema(name = "利息")
    private BigDecimal amountFee;
    @Column(name = "content")
    private String content;
}
