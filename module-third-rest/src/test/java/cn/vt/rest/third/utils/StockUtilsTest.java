package cn.vt.rest.third.utils;

import cn.vt.test.TestBase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StockUtils}.
 *
 * @author lei.liu
 * @since 2024-08-17
 */
public class StockUtilsTest extends TestBase {
    @Test
    public void testComputeTradeNumber() {
        int n = StockUtils.computeTradeQuantity(2000, 0.72, false);
        getLogger().info("n={}", n);
        int n2 = StockUtils.computeTradeQuantity(2000, 0.72, true);
        getLogger().info("n2={}", n2);
    }

}
