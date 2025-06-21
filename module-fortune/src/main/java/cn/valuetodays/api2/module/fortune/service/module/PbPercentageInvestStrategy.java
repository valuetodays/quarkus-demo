package cn.valuetodays.api2.module.fortune.service.module;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.persist.QuoteValuePO;
import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import cn.valuetodays.api2.module.fortune.service.QuoteValueServiceImpl;
import cn.valuetodays.quarkus.commons.base.jpa.StatDateAndLongIdEntity;
import cn.vt.util.ConvertUtils;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * 按pb百分位交易.
 * pb百分位为近10年的数据。
 * <p>
 * 低于30%：买入
 * 30%~70%：持有
 * 高于70%：卖出
 *
 * @author lei.liu
 * @since 2023-05-06 21:44
 */
@Slf4j
@ApplicationScoped
public class PbPercentageInvestStrategy implements InvestStrategy {
    @Inject
    private QuoteDailyStatServiceImpl quoteDailyStatService;
    @Inject
    private QuoteValueServiceImpl quoteValueService;

    @Override
    public InvestStrategyResp doInvest(InvestStrategyReq req) {
        String code = req.getStockCode();
        List<QuoteDailyStatPO> dataList = quoteDailyStatService.findAllByCodeOrderByMinTimeDesc(code);
        List<QuoteDailyStatPO> tradeDays = computeTradeDays(req, dataList);
        List<QuoteValuePO> indexValueList = quoteValueService.findAllByCode(code);
        QuoteDailyStatPO firstTradeDay = tradeDays.get(0);
        LocalDate firstTradeDate = InvestStrategy.fromYyyyMMdd(firstTradeDay.getStatDate());
        final LocalDate tenYearsAgo = firstTradeDate.minusYears(10);

        List<QuoteValuePO> sortedIndexValueList = indexValueList.stream()
            .sorted(Comparator.comparingInt(StatDateAndLongIdEntity::getStatDate))
            .filter(e -> tenYearsAgo.isBefore(DateUtils.formatYyyyMmDdAsLocalDateTime(e.getStatDate()).toLocalDate()))
            .toList();
        List<PercentagedQuoteValueVo> sortedPercentagedIndexValueList = new ArrayList<>(sortedIndexValueList.size());
        for (int i = 0; i < sortedIndexValueList.size(); i++) {
            QuoteValuePO tmp = sortedIndexValueList.get(i);
            PercentagedQuoteValueVo piv = new PercentagedQuoteValueVo();
            ConvertUtils.convertObj2(tmp, piv);
            // 1年按240个交易日
            final int daysInPeriod = 240;
            if (i >= daysInPeriod) {
                List<QuoteValuePO> latest10YearItems = sortedIndexValueList.subList(i - daysInPeriod, i);
                long lowerThanTodayPb = latest10YearItems.stream()
                    .filter(e -> e.getPbVal().compareTo(tmp.getPbVal()) <= 0)
                    .count();
                long lowerThanTodayPe = latest10YearItems.stream()
                    .filter(e -> e.getPeVal().compareTo(tmp.getPeVal()) <= 0)
                    .count();
                piv.setCurrentPbPercentage(lowerThanTodayPb * 10000.0 / latest10YearItems.size());
                piv.setCurrentPePercentage(lowerThanTodayPe * 10000.0 / latest10YearItems.size());
            }
            sortedPercentagedIndexValueList.add(piv);
        }
        Map<Integer, PercentagedQuoteValueVo> minTimeAndPercentagedIndevValueMap = sortedPercentagedIndexValueList.stream()
            .collect(Collectors.toMap(StatDateAndLongIdEntity::getStatDate, e -> e));

        // 每月预期投入金额
        double expectedUseMoneyPerMonth = 2000;
        // 总计投入金额
        double totalUsedMoney = 0;
        // 总计持有股数
        int totalShares = 0;
        //
        double fenhongMoney = 0;
        int fenhongShares = 0;

        List<InvestStrategyResp.Detail> details = new ArrayList<>(tradeDays.size());

        for (int i = 0; i < tradeDays.size(); i++) {
            QuoteDailyStatPO tradeDay = tradeDays.get(i);
            int statDateInTradeDay = tradeDay.getStatDate();
            InvestStrategyResp.Detail detail = new InvestStrategyResp.Detail();
            detail.setSeqNum(i + 1);
            detail.setMinTime(statDateInTradeDay);
            PercentagedQuoteValueVo percentagedIndexValueVo = null;
            final LocalDate localDate = InvestStrategy.fromYyyyMMdd(statDateInTradeDay);
            // 当指定的交易日不存在对应估值数据时，往后找10天
            for (int n = 0; n < 10; n++) {
                LocalDate tmpDate = localDate.plusDays(n);
                percentagedIndexValueVo = minTimeAndPercentagedIndevValueMap.get(InvestStrategy.toYyyyMMdd(tmpDate));
                if (Objects.nonNull(percentagedIndexValueVo)) {
                    break;
                }
            }
            InvestStrategyResp.Detail.Buy buy = new InvestStrategyResp.Detail.Buy();
            if (Objects.isNull(percentagedIndexValueVo)) {
                buy.setPrice(0);
                buy.setShares(0);
                buy.setTradeAmount(0);
                buy.setNote("无pb/pe百分比数据");
            } else {
                double closePx = tradeDay.getClosePx();
                // 价格 = 收盘点数/1000
                double price = closePx / 1000;
                int shares = (int) (expectedUseMoneyPerMonth / price);
                double currentPbPercentage = percentagedIndexValueVo.getCurrentPbPercentage();
                if (currentPbPercentage <= 0.31) {
                    buy.setPrice(price);
                    buy.setShares(shares);
                    totalShares += buy.getShares();
                    int moneyUsedInThisMonth = (int) Math.floor(price * shares);
                    buy.setTradeAmount(moneyUsedInThisMonth);
                    totalUsedMoney += buy.getTradeAmount();
                } else if (currentPbPercentage >= 0.71) {
                    // 手中有股才能卖出
                    if (totalShares >= shares) {
                        buy.setPrice(price);
                        buy.setShares(-1 * shares);
                    } else {
                        buy.setPrice(0);
                        buy.setShares(0);
                        buy.setNote("pb/pe > 0.71，待卖出，但是手中无股票");
                    }
                    totalShares += buy.getShares();
                    int moneyUsedInThisMonth = (int) Math.floor(price * buy.getShares());
                    buy.setTradeAmount(moneyUsedInThisMonth);
                    totalUsedMoney += buy.getTradeAmount();
                } else {
                    buy.setPrice(0);
                    buy.setShares(0);
                    buy.setTradeAmount(0);
                    buy.setNote("pb/pe 在0.31 和 0.71，无操作");
                }
            }
            detail.setBuy(buy);
            details.add(detail);
        }

        double currentMarketValue = tradeDays.get(tradeDays.size() - 1).getClosePx() / 1000 * totalShares;

        InvestStrategyResp resp = new InvestStrategyResp();
        resp.setReq(req);
        resp.setTimes(tradeDays.size());
        resp.setTotalUsedMoney(totalUsedMoney);
        resp.setTotalShares(totalShares);
        resp.setCurrentMarketValue(currentMarketValue);
        resp.setTotalBenefit((currentMarketValue - totalUsedMoney));
        resp.setDetails(details);
        return resp;
    } // end of doInvest()

    private List<QuoteDailyStatPO> computeTradeDays(InvestStrategyReq req, List<QuoteDailyStatPO> dataList) {
        LocalDate beginDate = req.getBeginDate();
        LocalDate endDate = req.getEndDate();

        int beginDateAsInt = InvestStrategy.toYyyyMMdd(beginDate);
        int endDateAsInt = InvestStrategy.toYyyyMMdd(endDate);
        List<QuoteDailyStatPO> tradeDays = new ArrayList<>();
        Map<Integer, QuoteDailyStatPO> statDateAndObjMap = dataList.stream()
            .collect(Collectors.toMap(QuoteDailyStatPO::getStatDate, e -> e));
        List<Integer> minTimeAscList = statDateAndObjMap.keySet().stream()
            .filter(e -> beginDateAsInt <= e && endDateAsInt >= e)
            .sorted()
            .toList();
        final int firstDayOfMonth = 1;

        while (beginDate.isBefore(endDate)) {
            if (beginDate.getDayOfMonth() == firstDayOfMonth) {
                int i;
                LocalDate tmp = beginDate;
                while (true) {
                    int yyyyMMdd = InvestStrategy.toYyyyMMdd(tmp);
                    i = minTimeAscList.indexOf(yyyyMMdd);
                    if (i > -1) {
                        break;
                    }
                    tmp = tmp.plusDays(1);
                }
                tradeDays.add(statDateAndObjMap.get(minTimeAscList.get(i)));
            }
            beginDate = beginDate.plusMonths(1).withDayOfMonth(firstDayOfMonth);
        }
        return tradeDays;
    }

}
