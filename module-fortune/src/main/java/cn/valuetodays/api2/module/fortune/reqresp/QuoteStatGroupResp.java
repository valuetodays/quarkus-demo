package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 指数统计结果组.
 *
 * @author lei.liu
 * @since 2023-05-10 15:46
 */
@Data
public class QuoteStatGroupResp implements Serializable {
    private List<QuoteStatResp> indexStatList;
}
