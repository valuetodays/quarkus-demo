package cn.vt.rest.third.eastmoney.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2022-05-31
 */
@Data
public class PagedData<T> implements Serializable {
    private int pages;
    private List<T> data;
}
