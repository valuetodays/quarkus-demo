package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-21
 */
@Data
public class SaveTradeMonitorByTradeResp implements Serializable {
    private String msg;
    private List<String> resultList;
}
