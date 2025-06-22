package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-11
 */
@Data
@Schema(name = "股票价格梯度-响应对象")
public class StockPriceGradientResp implements Serializable {
    @Schema(name = "股票代码", examples = {"SH600036", "SZ159605"})
    private String code;
    @Schema(name = "当前股价", examples = {"1.534", "0.678"})
    private BigDecimal currentPrice;
    @Schema(name = "梯度价格列表")
    private List<GradientItem> gradients;

    @Data
    @Schema(name = "梯度价格-对象")
    public static class GradientItem implements Serializable {
        @Schema(name = "股价", examples = {"1.534", "0.678"})
        private BigDecimal price;
        @Schema(name = "涨跌幅，前端直接加%即可使用", examples = {"-1.5", "0.67"})
        private BigDecimal chgPtg;
    }
}
