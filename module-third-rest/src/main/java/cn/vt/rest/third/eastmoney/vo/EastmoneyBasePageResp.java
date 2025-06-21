package cn.vt.rest.third.eastmoney.vo;

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
public abstract class EastmoneyBasePageResp<T> extends EastmoneyBaseResp {
    private PagedData<T> result;
}
