package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * .
 */
@Data
public class StockMinutePriceRefreshOneReq implements Serializable {
    @NotBlank(message = "code不能为空")
    // 600036
    private String code;
}
