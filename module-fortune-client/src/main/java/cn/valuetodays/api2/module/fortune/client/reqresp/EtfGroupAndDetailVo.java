package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-31 18:09
 */
@Data
public class EtfGroupAndDetailVo implements Serializable {
    private Long id;
    private String name;
    private int tn;
    private List<String> codes;
}
