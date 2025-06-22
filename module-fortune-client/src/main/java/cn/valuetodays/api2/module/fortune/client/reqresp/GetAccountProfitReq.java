package cn.valuetodays.api2.module.fortune.client.reqresp;

import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.web.req.BaseAccountableReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-12-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetAccountProfitReq extends BaseAccountableReq {
    private FortuneCommonEnums.Channel channel = FortuneCommonEnums.Channel.HAITONG;
}
