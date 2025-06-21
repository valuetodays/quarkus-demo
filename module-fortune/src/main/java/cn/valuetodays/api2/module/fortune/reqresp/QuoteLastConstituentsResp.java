package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteConstituentPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-15
 */
@Data
public class QuoteLastConstituentsResp implements Serializable {
    private QuotePO quote;
    private List<QuoteConstituentPO> quoteConstituents;
}
