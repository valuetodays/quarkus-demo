package cn.vt.rest.third.eastmoney.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2022-05-31
 */
@Data
public abstract class EastmoneyBaseResp implements Serializable {
    private String version;
    private boolean success;
    private String message;
    private int code;
}
