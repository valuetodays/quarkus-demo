package cn.valuetodays.api2.module.fortune.client.persist;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

import cn.valuetodays.quarkus.commons.base.jpa.JpaAccountableLongIdEntity;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-21
 */
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "fortune_stock_trade")
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.Data
public class StockTradePO extends JpaAccountableLongIdEntity {
    // 认输了，不再交易了。
    public static final Long GIVE_UP_OPPOSITE_TRADE = -99L;

    @Column(name = "code")
    private String code;
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private FortuneCommonEnums.Channel channel;
    @Column(name = "channel_order_id")
    private String channelOrderId;
    @Column(name = "trade_type")
    @Enumerated(EnumType.STRING)
    private FortuneCommonEnums.TradeType tradeType;
    @Column(name = "trade_date")
    private Integer tradeDate;
    @Column(name = "trade_time")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime tradeTime;
    @Column(name = "quantity")
    @Schema(description = "成交数量")
    private int quantity;
    @Column(name = "price")
    @Schema(description = "成交单价")
    private BigDecimal price;
    @Column(name = "yongjin_fee")
    @Schema(description = "佣金")
    private BigDecimal yongjinFee;
    @Column(name = "yinhua_fee")
    @Schema(description = "印花税")
    private BigDecimal yinhuaFee;
    @Column(name = "guohu_fee")
    @Schema(description = "过户费")
    private BigDecimal guohuFee;
    @Column(name = "hedge_id")
    @Schema(description = "对应的买单id")
    private Long hedgeId;
    @Column(name = "remark")
    private String remark;

    @JsonIgnore
    public boolean wasNotHedged() {
        return Objects.isNull(hedgeId) || hedgeId.equals(0L);
    }

    @JsonIgnore
    public boolean wasHedged() {
        return !wasNotHedged();
    }

    @JsonIgnore
    public boolean isGiveUpOppositeTrade() {
        return Objects.equals(hedgeId, StockTradePO.GIVE_UP_OPPOSITE_TRADE);
    }

    @JsonIgnore
    public boolean markedAsHedged() {
        return StringUtils.containsIgnoreCase(remark, "HEDGED");
    }
}
