package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteStatGroupResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteStatResp;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 指数统计模块.
 * <p>
 * 可以独立测试，不需要从db中获取数据
 *
 * @author lei.liu
 * @since 2023-05-12 12:30
 */
@ApplicationScoped
@Slf4j
public class QuoteStatModule {

    public QuoteStatGroupResp computeStats(List<QuoteDailyStatPO> list, LocalDate date) {
        QuoteStatGroupResp quoteStatGroupResp = new QuoteStatGroupResp();

        // 今年以来涨了多少？  相对近期高点（1.098）下跌了19.03%，相对于近期低点（0.742元）上涨了19.81%
        List<QuoteStatResp> quoteStatResps = new ArrayList<>();
        quoteStatResps.add(computeStatOfCurrentYear(list, date));
        quoteStatResps.add(computeStatOfLatest1year(list, date));
        quoteStatResps.add(computeStatOfLatest2Years(list, date));
        quoteStatResps.add(computeStatOfLatest3Years(list, date));
        quoteStatGroupResp.setIndexStatList(quoteStatResps);
        return quoteStatGroupResp;
    }

    public QuoteStatGroupResp computeStatsForToday(List<QuoteDailyStatPO> list) {
        return computeStats(list, LocalDate.now());
    }

    public QuoteStatResp computeStatOfLatest3Years(List<QuoteDailyStatPO> list, LocalDate today) {
        LocalDate pre12Months = today.minusYears(3);
        QuoteStatResp quoteStatResp = this.computeStat(list, pre12Months, today);
        quoteStatResp.setStatTitle("近3年涨幅");
        return quoteStatResp;
    }

    public QuoteStatResp computeStatOfLatest2Years(List<QuoteDailyStatPO> list, LocalDate today) {
        LocalDate pre12Months = today.minusYears(2);
        QuoteStatResp quoteStatResp = this.computeStat(list, pre12Months, today);
        quoteStatResp.setStatTitle("近2年涨幅");
        return quoteStatResp;
    }


    private QuoteStatResp computeStatOfLatest1year(List<QuoteDailyStatPO> list, LocalDate today) {
        LocalDate pre12Months = today.minusYears(1);
        QuoteStatResp quoteStatResp = this.computeStat(list, pre12Months, today);
        quoteStatResp.setStatTitle("近1年涨幅");
        return quoteStatResp;
    }

    private QuoteStatResp computeStatOfCurrentYear(List<QuoteDailyStatPO> list, LocalDate today) {
        LocalDate ltForCurrentYear = today.withDayOfYear(1);
        QuoteStatResp quoteStatResp = this.computeStat(list, ltForCurrentYear, today);
        quoteStatResp.setStatTitle("今年来涨幅");
        return quoteStatResp;
    }

    public QuoteStatResp computeStat(List<QuoteDailyStatPO> allList, LocalDate begin, LocalDate end) {
        QuoteStatResp resp = new QuoteStatResp();
        if (CollectionUtils.isEmpty(allList)
            || Objects.isNull(begin) || Objects.isNull(end) || begin.isAfter(end)) {
            resp.setStatTitle("无结果。原因：无数据或时间为空或开始时间在结束时间之后");
            return resp;
        }
        int yyyyMMddForBegin = InvestStrategy.toYyyyMMdd(begin);
        int yyyyMMddForEnd = InvestStrategy.toYyyyMMdd(end);
        List<QuoteDailyStatPO> sortedList = allList.stream()
            .sorted(Comparator.comparingInt(QuoteDailyStatPO::getStatDate))
            .toList();
        List<QuoteDailyStatPO> candicateList = sortedList.stream()
            .filter(e -> e.getStatDate() >= yyyyMMddForBegin && e.getStatDate() <= yyyyMMddForEnd)
            .toList();
        if (CollectionUtils.isEmpty(candicateList)) {
            resp.setStatTitle("没有符合时间条件的数据");
            return resp;
        }
        QuoteDailyStatPO first = candicateList.get(0);
        Double openPx = first.getOpenPx();
        QuoteDailyStatPO last = candicateList.get(candicateList.size() - 1);
        Double closePx = last.getClosePx();

        double highestPx = candicateList.stream()
            .mapToDouble(QuoteDailyStatPO::getHighPx)
            .max()
            .orElse(openPx);
        double lowestPx = candicateList.stream()
            .mapToDouble(QuoteDailyStatPO::getLowPx)
            .min()
            .orElse(openPx);

        resp.setOpenPx(openPx);
        resp.setCurrentClosePx(closePx);
        resp.setHighestPx(highestPx);
        resp.setLowestPx(lowestPx);
        resp.setBeginStatMinTime(first.getStatDate());
        resp.setEndStatMinTime(last.getStatDate());
        resp.computePercentage();
        return resp;
    }

    public DailyOffsetStatResp computeDailyOffsetStat(List<QuoteDailyStatPO> list) {
        List<Double> dailyOffsetList = list.stream()
            .mapToDouble(e ->
                BigDecimal.valueOf(e.getHighPx()).subtract(BigDecimal.valueOf(e.getLowPx()))
                    .setScale(4, RoundingMode.UP)
                    .doubleValue()
            ).boxed().toList();
        int size = dailyOffsetList.size();
        List<Double> distinctList = dailyOffsetList.stream().distinct().sorted().toList();
        Map<Double, Double> offsetAndHistoryPtg = distinctList.stream()
            .collect(
                Collectors.toMap(e -> e,
                    e -> dailyOffsetList.stream().filter(m -> e.compareTo(m) >= 0).count() * 100.0 / size
                )
            );
//        TreeMap<Double, Double> doubleDoubleTreeMap = new TreeMap<>(offsetAndHistoryPtg);
//        for (Map.Entry<Double, Double> e : doubleDoubleTreeMap.entrySet()) {
//            System.out.println(e.getKey() + " --> " + e.getValue());
//        }
        DoubleStream doubleStream = dailyOffsetList.stream().mapToDouble(e -> e);
        // 当日最高价与最低低差值的最大值、最小值、平均值
        DoubleSummaryStatistics dss = doubleStream.summaryStatistics();
        double max = dss.getMax();
        double min = dss.getMin();
        double maxOffset = max - min;
        double ptgValue = BigDecimal.valueOf(maxOffset).multiply(BigDecimal.valueOf(30.0 / 100)).setScale(4, RoundingMode.UP).doubleValue();
        Double higherLine = BigDecimal.valueOf(max).subtract(BigDecimal.valueOf(ptgValue)).doubleValue();
        Double lowerLine = BigDecimal.valueOf(min).add(BigDecimal.valueOf(ptgValue)).doubleValue();

        double higherLinePtg = dailyOffsetList.stream().filter(m -> higherLine.compareTo(m) >= 0).count() * 100.0 / size;
        double lowerLinePtg = dailyOffsetList.stream().filter(m -> lowerLine.compareTo(m) >= 0).count() * 100.0 / size;

        DailyOffsetStatResp resp = new DailyOffsetStatResp();
        resp.setMax(max);
        resp.setMin(min);
        resp.setAverage(dss.getAverage());
//        resp.setOffsetAndHistoryPtg(offsetAndHistoryPtg);
        resp.setHigherLine(higherLine);
        resp.setHigherLinePtg(higherLinePtg);
        resp.setLowerLine(lowerLine);
        resp.setLowerLinePtg(lowerLinePtg);
        return resp;
    }
}
