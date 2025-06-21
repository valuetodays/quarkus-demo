package cn.valuetodays.api2.module.fortune.service.module;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-05 11:52
 */
@Data
public class InvestStrategyReq implements Serializable {
    private Type type = Type.FIX;
    // 每月预期投入金额
    private double expectedUseMoneyPerMonth = 2000;
    private String title;
    private String description;
    private String stockCode;
    private LocalDate beginDate;
    private LocalDate endDate;
    public enum Type {
        FIX, PB, LATEST3YEAR
    }
}
