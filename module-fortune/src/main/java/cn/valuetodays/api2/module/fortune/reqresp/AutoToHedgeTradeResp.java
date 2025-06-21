package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-01-31
 */
@Data
public class AutoToHedgeTradeResp implements Serializable {
    private int records; // 总条数
    private int matchedRecords; // 匹配的条数，可能是奇数
    private String msg;
    private Map<Long, Long> idMap = Map.of(); // id -> hedgeId

    @JsonIgnore
    public AutoToHedgeTradeResp merge(AutoToHedgeTradeResp another) {
        AutoToHedgeTradeResp newResp = new AutoToHedgeTradeResp();
        newResp.setRecords(records + another.getRecords());
        newResp.setMatchedRecords(matchedRecords + another.getMatchedRecords());
        return newResp;
    }

}
