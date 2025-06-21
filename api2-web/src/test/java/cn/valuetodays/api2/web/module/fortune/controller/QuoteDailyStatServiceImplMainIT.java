package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import cn.valuetodays.api2.module.fortune.service.kits.IndexKeyPointComputer;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockKlineData;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockKlineResp;
import cn.vt.test.TestBase;
import cn.vt.util.DateUtils;
import cn.vt.util.JsonUtils;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-02 13:40
 */
class QuoteDailyStatServiceImplMainIT extends TestBase {

    @Inject
    QuoteDailyStatServiceImpl quoteDailyStatService;

    @Test
    void refreshOne() {
        String jsonStr = super.getFileAsString("000001-ss-index-data.txt");

        EastMoneyStockKlineResp klineResp = JsonUtils.fromJson(jsonStr, EastMoneyStockKlineResp.class);
        EastMoneyStockKlineData data = klineResp.getData();
        String[] klines = data.getKlines();
        getLog().info("klines: {}", klines.length);
        List<QuoteDailyStatPO> list = Arrays.stream(klines)
            .map(QuoteDailyStatServiceImpl.TRANSFORMER)
            .toList();
        QuoteDailyStatPO last = list.get(list.size() - 1);
        getLog().info("last: {}", last);
        Assertions.assertNotNull(last);
    }

    @Test
    public void testIntervalDay() {
        // 计算两日期间的相差天数
        LocalDate ld = LocalDate.of(1979, 1, 1);
        long days = ChronoUnit.DAYS.between(ld, LocalDate.now());
        getLog().info("days: {}", days);
        Assertions.assertTrue(days > 0);
        long days2 = DateUtils.intervalDays(ld);
        Assertions.assertEquals(days, days2);
    }

    @Test
    public void determineHighestAndLowestFor000300() {
        // 测试沪深300
        String code = "000300";
        List<QuoteDailyStatPO> list = quoteDailyStatService.findAllByCodeOrderByMinTimeDesc(code);
        getLog().info("list.size: {}", list.size());

        // 涨跌幅10%以内数据都舍弃
        double offsetPercentage = 0.1;

        IndexKeyPointComputer strategy = new IndexKeyPointComputer();
        List<QuoteDailyStatPO> keyPointList = strategy.doCompute(list, offsetPercentage);

        getLog().info("keyPointList.size={}", keyPointList.size());
        for (int i = 1; i < keyPointList.size(); i++) {
            QuoteDailyStatPO pre = keyPointList.get(i - 1);
            Double preHighPx = pre.getHighPx();
            Double preLowPx = pre.getLowPx();
            QuoteDailyStatPO current = keyPointList.get(i);
            Double currentHighPx = current.getHighPx();
            Double currentLowPx = current.getLowPx();
            int compareTo = preHighPx.compareTo(currentLowPx);
            if (compareTo > 0) {
                BigDecimal value = BigDecimal.valueOf(preHighPx - currentLowPx).divide(BigDecimal.valueOf(preHighPx), 2, RoundingMode.DOWN);
                getLog().info("minTime {} -> {} 跌了 {}%", pre.getStatDate(), current.getStatDate(), value.doubleValue() * 100);
            } else {
                BigDecimal value = BigDecimal.valueOf(preLowPx - currentHighPx).abs().divide(BigDecimal.valueOf(preLowPx), 2, RoundingMode.DOWN);
                getLog().info("minTime {} -> {} 涨了 {}%", pre.getStatDate(), current.getStatDate(), value.doubleValue() * 100);
            }
        }
        // todo 统计：
        // 从最高点跌了多少
        // 自最低点涨了多少
        // 从前一高点跌了多少
        // 自前一低点涨了多少
    }

}
