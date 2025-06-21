package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-03
 */
@Data
public class CheckHedgedKitsResp implements Serializable {
    private List<Result> resultList;

    public static Result ofResult(String ids, String msg) {
        Result result = new Result();
        result.setIds(ids);
        result.setErrorMsg(msg);
        return result;
    }

    @Data
    public static class Result implements Serializable {
        // 数据的ids
        private String ids;
        // 错误日志
        private String errorMsg;
    }
}
