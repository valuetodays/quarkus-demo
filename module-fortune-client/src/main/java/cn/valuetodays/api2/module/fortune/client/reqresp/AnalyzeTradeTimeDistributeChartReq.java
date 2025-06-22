package cn.valuetodays.api2.module.fortune.client.reqresp;

import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.web.req.BaseAccountableReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-08-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AnalyzeTradeTimeDistributeChartReq extends BaseAccountableReq {
    private FortuneCommonEnums.Channel channel = FortuneCommonEnums.Channel.ALL;
    private String code;
}
