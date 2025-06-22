package cn.valuetodays.api2.module.fortune.client.reqresp;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * .
 *
 * @author lei.liu
 * @since 2022-05-31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class EastmoneyBaseObjResp<T> extends EastmoneyBaseResp {
    private T result;
}
