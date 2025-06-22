package cn.valuetodays.api2.module.fortune.client.persist;

import cn.valuetodays.api2.module.fortune.client.enums.StockHistoryPriceEnums;
import cn.valuetodays.quarkus.commons.base.jpa.JpaCrudLongIdBasePersist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "股票历史价格")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"hibernateLazyInitializer", "handler", "fieldHandler"}
)
@Entity
@Table(name = "fortune_stock_history_price")
@Data
public class StockHistoryPricePO extends JpaCrudLongIdBasePersist {
    @Column(name = "code")
    private String code;
    @Column(name = "region")
    private String region;
    @Schema(description = "区域")
    @Enumerated(EnumType.STRING)
    private StockHistoryPriceEnums.Channel channel;
    @Column(name = "min_time")
    private int minTime; // 时间 yyyyMMdd 20121221
    @Column(name = "open_px")
    private double openPx; // 开盘价 2.396
    @Column(name = "high_px")
    private double highPx; // 最高价 2.413
    @Column(name = "low_px")
    private double lowPx;  // 最低价 2.366
    @Column(name = "close_px")
    private double closePx; // 收盘价 2.378
    @Column(name = "business_amount")
    private long businessAmount; // 成交量 419359410

}
