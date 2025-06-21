package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.EtfInfoPO;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-10 16:53
 */
@Data
public class CodedQuoteStatVo implements Serializable {
    private String code;
    private String name;
    private List<EtfInfoPO> suggestEtfs;
    private QuoteStatGroupResp groupResp;
}
