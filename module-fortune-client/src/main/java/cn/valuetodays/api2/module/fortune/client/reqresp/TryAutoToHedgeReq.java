package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-12
 */
@Data
public class TryAutoToHedgeReq implements Serializable {
    private String code;
    private boolean dryRun = true;
}
