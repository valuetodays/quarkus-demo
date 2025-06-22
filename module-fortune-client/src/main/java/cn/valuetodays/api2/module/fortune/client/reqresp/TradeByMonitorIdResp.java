package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-01
 */
@Data
public class TradeByMonitorIdResp implements Serializable {
    private int n;
    private String msg;

    public static TradeByMonitorIdResp empty(String msg) {
        TradeByMonitorIdResp resp = new TradeByMonitorIdResp();
        resp.setN(0);
        resp.setMsg(msg);
        return resp;
    }

    public static TradeByMonitorIdResp of(int n) {
        TradeByMonitorIdResp resp = new TradeByMonitorIdResp();
        resp.setN(n);
        return resp;
    }
}
