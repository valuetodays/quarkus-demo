package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-30
 */
@Data
public class SaveAllHoldersReq implements Serializable {
    // yyyyMMdd;
    private int date;
    @NotNull(message = "渠道不能为空")
    private FortuneCommonEnums.Channel channel;
    private List<HolderInfo> holderInfos;
}
