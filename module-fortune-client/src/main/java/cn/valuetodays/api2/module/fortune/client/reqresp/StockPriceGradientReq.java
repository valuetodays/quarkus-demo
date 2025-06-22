package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-11
 */
@Data
@Schema(name = "股票价格梯度-请求对象")
public class StockPriceGradientReq implements Serializable {
    @Schema(name = "股票代码", examples = {"600036", "159605"})
    @NotNull
    private String code;
    @Schema(name = "范围值", examples = {"-2", "-1.5", "1", "1.5", "2", "2.8"})
    private BigDecimal rangeValue;
}
