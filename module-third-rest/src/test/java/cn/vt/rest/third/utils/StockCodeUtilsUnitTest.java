package cn.vt.rest.third.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StockCodeUtils}.
 *
 * @author lei.liu
 * @since 2024-04-04
 */
public class StockCodeUtilsUnitTest {

    @Test
    public void buildForXueQiu() {
        String s = StockCodeUtils.buildForXueQiu("600036");
        Assertions.assertEquals("SH600036", s);
        s = StockCodeUtils.buildForXueQiu("SH600036");
        Assertions.assertEquals("SH600036", s);
    }

    @Test
    public void buildForEhaifangzhou() {
        String s = StockCodeUtils.buildForEhaifangzhou("600036");
        Assertions.assertEquals("600036.SH", s);
        s = StockCodeUtils.buildForEhaifangzhou("600036.SH");
        Assertions.assertEquals("600036.SH", s);
    }

    @Test
    public void parseFromEhaifangzhou() {
        String s = StockCodeUtils.parseFromEhaifangzhou("600036.SH");
        Assertions.assertEquals("600036", s);
        s = StockCodeUtils.parseFromEhaifangzhou("600036");
        Assertions.assertEquals("600036", s);
    }
}
