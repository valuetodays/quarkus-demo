package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.math.BigDecimal;

import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-10-10
 */
@Data
public class StatEarnedResp implements Serializable {
    private FortuneCommonEnums.Channel channel;
    private BigDecimal money;
}
