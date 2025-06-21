package cn.vt.rest.third.eastmoney.vo;

import java.util.List;

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
public abstract class EastmoneyBaseListResp<T> extends EastmoneyBaseResp {
    private List<T> result;
}
