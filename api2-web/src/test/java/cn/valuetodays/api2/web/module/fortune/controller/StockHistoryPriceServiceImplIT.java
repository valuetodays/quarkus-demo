package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockHistoryPricePO;
import cn.valuetodays.api2.module.fortune.client.persist.StockPO;
import cn.valuetodays.api2.module.fortune.service.StockHistoryPriceServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockServiceImpl;
import cn.vt.rest.third.quote.CookieClientUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author lei.liu
 * @since 2021-06-16 16:45
 */
@Slf4j
public class StockHistoryPriceServiceImplIT {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Inject
    StockServiceImpl stockService;
    @Inject
    StockHistoryPriceServiceImpl stockHistoryPriceService;

    private static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal toChangeRadio(double value1, double value2) {
        BigDecimal v1 = toBigDecimal(value1);
        BigDecimal v2 = toBigDecimal(value2);
        return (v1.subtract(v2)).divide(v2, RoundingMode.HALF_UP).multiply(ONE_HUNDRED);
    }

    @Test
    public void saveAll() {
        String loginCookie = CookieClientUtils.pullXueqiuCookies();
        stockHistoryPriceService.refresh(loginCookie);
    }

    @Test
    public void saveOne() {
        String loginCookie = CookieClientUtils.pullXueqiuCookies();
        StockPO stockPO = stockService.findByCode("");
        stockHistoryPriceService.refreshOne(stockPO, loginCookie);
    }

    @Test
    public void computeOffsetDistribution() {
        String v = stockHistoryPriceService.computeOffsetDistribution("513300");
        log.info("v={}", v);
    }

    @Test
    public void showStockList() {
        List<StockPO> stockList = stockService.list();
        log.info("stockList: {}", stockList);
    }

    private List<StockHistoryPricePO> getSortedList(String code) {
        return getSortedList(code, false);
    }

    private List<StockHistoryPricePO> getSortedList(String code, boolean latestAtFirst) {
        Comparator<StockHistoryPricePO> comparator = Comparator.comparingInt(StockHistoryPricePO::getMinTime);
        if (latestAtFirst) {
            comparator = comparator.reversed();
        }
        List<StockHistoryPricePO> historyPriceList = stockHistoryPriceService.findAllByCode(code);
        return historyPriceList.stream()
            .sorted(comparator)
            .toList();
    }

    /**
     * 根据每日收盘价计算出相对于前一日的涨跌幅
     */
    @Test
    public void parseChangeRadio() {
        String code = "159605";
        List<StockHistoryPricePO> sortedHistoryPriceList = getSortedList(code);
        StringBuilder sb = new StringBuilder(100000);
        int size = sortedHistoryPriceList.size();
        for (int i = 0; i < size; i++) {
            StockHistoryPricePO e = sortedHistoryPriceList.get(i);
            BigDecimal changeRadio = BigDecimal.ZERO;
            double closePx = e.getClosePx();
            if (i > 0) {
                StockHistoryPricePO pre = sortedHistoryPriceList.get(i - 1);
                double preClosePx = pre.getClosePx();
                changeRadio = toChangeRadio(closePx, preClosePx);
            }
            sb.append(e.getMinTime()).append("\t")
                .append(closePx).append("\t")
                .append(changeRadio).append("%\t")
                .append(e.getOpenPx()).append("\t")
                .append(e.getHighPx()).append("\t")
                .append(e.getLowPx()).append("\t")
                .append(toChangeRadio(e.getHighPx(), e.getOpenPx())).append("%\t")
                .append(toChangeRadio(e.getLowPx(), e.getOpenPx())).append("%\t")
                .append("\n");
        }
        log.info(">>\n{}", sb);
    }

    /**
     * 测试定投
     */
    @Test
    public void testFixedInvest() {
        String code = "512880";
        int totalDays = 5 * 250; // 5年
        int intervalDays = 1; // 间隔：每天=1，每周=5，每双周=10，每月=30
        int investDays = totalDays / intervalDays;
        int money = 1000; // 每期金额
        final BigDecimal moneyBigDecimal = BigDecimal.valueOf(money);
        List<StockHistoryPricePO> sortedHistoryPriceList = getSortedList(code, true);
        List<StockHistoryPricePO> dataList = sortedHistoryPriceList;
        int size = sortedHistoryPriceList.size();
        if (size > totalDays) {
            dataList = sortedHistoryPriceList.subList(0, totalDays);
        }
        BigDecimal count = BigDecimal.ZERO;
        BigDecimal inputMoney = BigDecimal.ZERO;
        int realSize = dataList.size();
        for (int i = 0; i < realSize; i += intervalDays) {
            StockHistoryPricePO data = dataList.get(i);
            count = count.add(
                moneyBigDecimal.divide(BigDecimal.valueOf(data.getClosePx()), 4, RoundingMode.CEILING)
            );
            inputMoney = inputMoney.add(moneyBigDecimal);
        }
        StockHistoryPricePO latest = dataList.get(realSize - 1);
        double closePx = latest.getClosePx();
        BigDecimal totalMoney = BigDecimal.valueOf(closePx).multiply(count);
        StringBuilder sb = new StringBuilder();
        sb
            .append("----------------").append("\n")
            .append("code: ").append(code).append("\n")
            .append("times: ").append(totalDays).append("\n")
            .append("count: ").append(count.doubleValue()).append("\n")
            .append("input: ").append(inputMoney.doubleValue()).append("\n")
            .append("total: ").append(totalMoney.doubleValue()).append("\n")
            .append("----------------").append("\n");
        log.info("\n{}", sb);
    }

    /**
     * 目前得出的结论是每天/每两天/每周/每双周交易，都会赔钱。
     */
    @Test
    public void buyAndSellEveryday() {
        String code = "510500";
        int intervalDays = 20; // 每天交易-1，每两天交易-2，每周交易-5，每双周交易-10
        final BigDecimal countPerDay = toBigDecimal(300);
        final BigDecimal initialMoney = BigDecimal.valueOf(30000);
        BigDecimal totalMoney = initialMoney;
        final BigDecimal initialStock = BigDecimal.valueOf(3000);
        BigDecimal totalStock = initialStock;
        final BigDecimal buyRadio = BigDecimal.valueOf(0.005);
        final BigDecimal sellRadio = BigDecimal.valueOf(0.007);
        StringBuilder sb = new StringBuilder(100000);
        List<StockHistoryPricePO> sortedHistoryPriceList = getSortedList(code, false);
        int size = sortedHistoryPriceList.size();
        for (int i = 0; i < size; i += intervalDays) {
            StockHistoryPricePO stockHistoryPricePO = sortedHistoryPriceList.get(i);
            int minTime = stockHistoryPricePO.getMinTime();
            BigDecimal openPx = toBigDecimal(stockHistoryPricePO.getOpenPx());
            BigDecimal highPx = toBigDecimal(stockHistoryPricePO.getHighPx());
            BigDecimal lowPx = toBigDecimal(stockHistoryPricePO.getLowPx());
            BigDecimal buyPrice = openPx.subtract(openPx.multiply(buyRadio));
            BigDecimal sellPrice = openPx.add(openPx.multiply(sellRadio));

            sb.append(minTime).append("\t")
                .append(openPx).append("\t")
                .append(highPx).append("\t")
                .append(lowPx).append("\t")
                .append(sellPrice).append("\t")
                .append(buyPrice).append("\t");

            if (lowPx.compareTo(buyPrice) < 1) {
                BigDecimal moneyForBuy = buyPrice.multiply(countPerDay);
                if (totalMoney.compareTo(moneyForBuy) >= 0) {
                    totalStock = totalStock.add(countPerDay);
                    totalMoney = totalMoney.subtract(moneyForBuy);
                    sb.append("buy-suc").append("\t");
                } else {
                    sb.append("buy-fail-no-enough-money").append("\t");
                }
            } else {
                sb.append("buy-fail").append("\t");
            }
            if (highPx.compareTo(sellPrice) > -1) {
                if (totalStock.compareTo(countPerDay) >= 0) {
                    BigDecimal moneyForSell = sellPrice.multiply(countPerDay);
                    totalStock = totalStock.subtract(countPerDay);
                    totalMoney = totalMoney.add(moneyForSell);
                    sb.append("sell-suc").append("\t");
                } else {
                    sb.append("sell-fail-no-enough-stock").append("\t");
                }
            } else {
                sb.append("sell-fail").append("\t");
            }
            sb.append(totalStock).append("\t")
                .append(totalMoney).append("\n");
        }
        log.info(">\n{}", sb);
    }

}
