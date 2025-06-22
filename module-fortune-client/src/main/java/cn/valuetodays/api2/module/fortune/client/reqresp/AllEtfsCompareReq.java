package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-31 19:51
 */
@Data
public class AllEtfsCompareReq implements Serializable {
    private int minMoneyPerTrade;
    // -1 = all, 1=t1
    private int tn = -1;
    private List<EtfsCompareReq> reqs;
}
