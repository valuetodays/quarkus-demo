package cn.vt.rest.third.eastmoney.vo;

import java.math.BigDecimal;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link StockDetailVO}.
 *
 * @author lei.liu
 * @since 2024-08-17
 */
public class StockDetailVOTest {

    @Test
    public void computeSuggestWithSell() {
        StockDetailVO sd = new StockDetailVO();
        sd.setPrice(new BigDecimal("1.093"));
        sd.setAvgPrice(new BigDecimal("1.086"));
        sd.setWaiPan(new BigDecimal(335));
        sd.setNeiPan(new BigDecimal(505));
        sd.computeSuggest();
        String suggest = sd.getSuggest();
        assertThat(suggest, IsNull.notNullValue());
        assertThat(suggest, IsEqual.equalTo("sell"));
    }

    @Test
    public void computeSuggestWithBuy() {
        StockDetailVO sd = new StockDetailVO();
        sd.setAvgPrice(new BigDecimal("1.093"));
        sd.setPrice(new BigDecimal("1.086"));
        sd.setNeiPan(new BigDecimal(335));
        sd.setWaiPan(new BigDecimal(505));
        sd.computeSuggest();
        String suggest = sd.getSuggest();
        assertThat(suggest, IsNull.notNullValue());
        assertThat(suggest, IsEqual.equalTo("buy"));
    }
}
