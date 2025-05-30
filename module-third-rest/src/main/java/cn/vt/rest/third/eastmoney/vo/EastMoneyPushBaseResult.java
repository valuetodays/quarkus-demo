package cn.vt.rest.third.eastmoney.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-27
 */
@Data
public abstract class EastMoneyPushBaseResult<T> implements Serializable {
    private int rc;
    private int rt;
    private long svr;
    private int lt;
    private int full;
    private String dlmkts;
    private T data;
}
