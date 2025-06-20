package cn.valuetodays.api2.module.fortune.persist;

import java.time.LocalDateTime;

import cn.valuetodays.api2.module.fortune.enums.StockHistoryPriceEnums;
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

@Schema(name = "股票分时价格")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"hibernateLazyInitializer", "handler", "fieldHandler"}
)
@Entity
@Table(name = "fortune_stock_minute_price")
@Data
public class StockMinutePricePO extends JpaCrudLongIdBasePersist {
    @Column(name = "code")
    private String code;
    @Enumerated(EnumType.STRING)
    private StockHistoryPriceEnums.Channel channel;
    @Column(name = "ts")
    private LocalDateTime ts;
    @Column(name = "avg_px")
    private Double avgPx;
    @Column(name = "close_px")
    private Double closePx;

    public StockMinutePricePO() {
    }

    public StockMinutePricePO(LocalDateTime ts, Double avgPx, Double closePx) {
        this.ts = ts;
        this.avgPx = avgPx;
        this.closePx = closePx;
    }
}
