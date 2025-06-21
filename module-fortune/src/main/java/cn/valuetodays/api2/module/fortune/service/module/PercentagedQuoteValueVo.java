package cn.valuetodays.api2.module.fortune.service.module;

import cn.valuetodays.api2.module.fortune.persist.QuoteValuePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-06 21:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PercentagedQuoteValueVo extends QuoteValuePO {
    private double currentPePercentage;
    private double currentPbPercentage;
}
