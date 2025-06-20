package cn.valuetodays.api2.web;

import java.math.RoundingMode;
import java.text.NumberFormat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-04-16
 */
public class NumberFormatTest {
    @Test
    void test() {
        Double totalAShareByYi = 12345.67;
        Double preTotalByYi = 11234.12;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(0);
        nf.setRoundingMode(RoundingMode.UP);
        String offsetStr = nf.format(totalAShareByYi - preTotalByYi);
        assertTrue(offsetStr.contains(","));
        nf.setGroupingUsed(false); // 关闭分组，显示将不再以千位符分隔
        offsetStr = nf.format(totalAShareByYi - preTotalByYi);
        assertFalse(offsetStr.contains(","));
    }
}
