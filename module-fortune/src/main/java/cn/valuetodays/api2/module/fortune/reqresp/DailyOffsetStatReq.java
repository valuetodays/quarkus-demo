package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * 指数统计结果.
 *
 * @author lei.liu
 * @since 2023-05-10 15:46
 */
@Data
public class DailyOffsetStatReq implements Serializable {
    private String code;

}
