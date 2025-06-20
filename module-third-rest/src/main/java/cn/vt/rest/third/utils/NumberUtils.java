package cn.vt.rest.third.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-20 22:58
 */
public final class NumberUtils {
    public static final BigDecimal WAN = BigDecimal.valueOf(10000.0D);
    public static final BigDecimal YI = BigDecimal.valueOf(100000000.0D);
    public static final BigDecimal WAN_YI = BigDecimal.valueOf(10000.0D).multiply(YI);
    ;
    private NumberUtils() {
    }

    public static BigDecimal divide2(BigDecimal number1, BigDecimal number2) {
        return number1.divide(number2, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide4(BigDecimal number1, BigDecimal number2) {
        return number1.divide(number2, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide4(BigDecimal number1, double number2) {
        return divide4(number1, BigDecimal.valueOf(number2));
    }

    public static BigDecimal divide4(double number1, double number2) {
        return divide4(BigDecimal.valueOf(number1), BigDecimal.valueOf(number2));
    }

    public static double divide3(BigDecimal number1, BigDecimal number2) {
        return number1.divide(number2, 3, RoundingMode.HALF_UP).doubleValue();
    }

    public static double divide3(BigDecimal number1, double number2) {
        return divide3(number1, BigDecimal.valueOf(number2));
    }

    public static double divide3(double number1, double number2) {
        return divide3(BigDecimal.valueOf(number1), BigDecimal.valueOf(number2));
    }

    public static BigDecimal computeByWanYi(BigDecimal value) {
        return divide4(value, WAN_YI);
    }

    public static BigDecimal computeByYi(BigDecimal value) {
        return computeBy(value, YI);
    }

    public static BigDecimal computeByWan(BigDecimal value) {
        return computeBy(value, WAN);
    }

    /* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */

    /**
     * Divide {@code value} by {@code unit} and round to 2 decimal places.
     *
     * @param value the value to be divided
     * @param unit  the unit to divide by
     * @return the result of the division, rounded to 2 decimal places
     */
    /* <<<<<<<<<<  e1e63aeb-63fe-4994-9cb0-37e7f2b46cab  >>>>>>>>>>> */
    public static BigDecimal computeBy(BigDecimal value, BigDecimal unit) {
        if (Objects.isNull(value)) {
            return BigDecimal.ZERO;
        }
        return divide2(value, unit);
    }

}
