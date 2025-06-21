package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-15
 */
@Data
public class QuoteLastConstituentsReq implements Serializable {
    private String quoteCode;
}
