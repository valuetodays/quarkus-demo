package cn.valuetodays.api2.module.fortune.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

import cn.valuetodays.api2.module.fortune.client.enums.StockHistoryPriceEnums;
import cn.valuetodays.api2.module.fortune.client.enums.StockSubjectEnums;
import cn.valuetodays.api2.module.fortune.client.persist.StockHistoryPricePO;
import cn.valuetodays.api2.module.fortune.client.persist.StockPO;
import cn.valuetodays.api2.module.fortune.client.persist.StockSubjectPO;
import cn.valuetodays.api2.module.fortune.client.reqresp.offset.StockBiasOffset;
import cn.valuetodays.api2.module.fortune.dao.StockHistoryPriceDAO;
import cn.valuetodays.api2.web.ICookieCacheComponent;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.exception.CommonException;
import cn.vt.rest.third.StockEnums;
import cn.vt.rest.third.eastmoney.vo.QuoteHistoryIndexRootData;
import cn.vt.rest.third.utils.StockUtils;
import cn.vt.rest.third.xueqiu.XueQiuStockClientUtils;
import cn.vt.rest.third.xueqiu.vo.PushCookieReq;
import cn.vt.rest.third.xueqiu.vo.XueQiuKlineData;
import cn.vt.rest.third.xueqiu.vo.XueQiuKlineResp;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * @author lei.liu
 * @since 2021-03-19 18:21
 */
@ApplicationScoped
@Slf4j
public class StockHistoryPriceServiceImpl
    extends BaseCrudService<Long, StockHistoryPricePO, StockHistoryPriceDAO> {

    @Inject
    StockServiceImpl stockService;
    @Inject
    ICookieCacheComponent cookieCacheComponentWrapper;
    @Inject
    StockSubjectServiceImpl stockSubjectService;

    public static List<StockHistoryPricePO> deal(XueQiuKlineData klineData, String region) {
        String[] column = klineData.getColumn();
        String[][] item = klineData.getItem();
        String symbol = klineData.getSymbol();
        if (StringUtils.isBlank(symbol)) {
            return null;
        }
        String code = symbol.substring(region.length());

        int timestampInx = ArrayUtils.indexOf(column, "timestamp");
        int openInx = ArrayUtils.indexOf(column, "open");
        int highInx = ArrayUtils.indexOf(column, "high");
        int lowInx = ArrayUtils.indexOf(column, "low");
        int closeInx = ArrayUtils.indexOf(column, "close");
        List<StockHistoryPricePO> toSaveList = new ArrayList<>(2000);
        for (String[] strings : item) {
            String timestampStr = strings[timestampInx];
            LocalDateTime today = DateUtils.getToday(Long.parseLong(timestampStr));
            final String format = DateUtils.DEFAULT_DATE_FORMAT.replace("-", "");
            String minTimeStr = DateUtils.formatDate(today, format);
            int minTime = Integer.parseInt(minTimeStr);
            String openStr = strings[openInx];
            String highStr = strings[highInx];
            String lowStr = strings[lowInx];
            String closeStr = strings[closeInx];
//            String todayTimeStr = DateUtils.formatDate(DateUtils.getToday(), format);
//            if (minTimeStr.equals(todayTimeStr)) {
//                continue;
//            }
            StockHistoryPricePO toSave = new StockHistoryPricePO();
            toSave.setChannel(StockHistoryPriceEnums.Channel.XUEQIU);
            toSave.setBusinessAmount(1);
            toSave.setCode(code);
            toSave.setRegion(region);
            toSave.setMinTime(minTime);
            toSave.setClosePx(Double.parseDouble(closeStr));
            toSave.setHighPx(Double.parseDouble(highStr));
            toSave.setLowPx(Double.parseDouble(lowStr));
            toSave.setOpenPx(Double.parseDouble(openStr));
            toSave.initUserIdAndTime(1L);
            toSaveList.add(toSave);
//            getLog().info("{},{},{},{},{}", minTime, openStr, highStr, lowStr, closeStr);
        }
        return toSaveList;
    }

    private static List<StockHistoryPricePO> deal(QuoteHistoryIndexRootData indexRootData) {
        QuoteHistoryIndexRootData.DataBean data = indexRootData.getData();
        QuoteHistoryIndexRootData.DataBean.CandleBean candle = data.getCandle();
        List<List<String>> history = candle.getHistory();
        List<StockHistoryPricePO> list
            = new ArrayList<>((int) (history.size() / 1.5f) + 1);
        for (List<String> strings : history) {
            StockHistoryPricePO stockHistoryPricePO = new StockHistoryPricePO();
            stockHistoryPricePO.setMinTime(Integer.parseInt(strings.get(0)));
            stockHistoryPricePO.setOpenPx(Double.parseDouble(strings.get(1)));
            stockHistoryPricePO.setHighPx(Double.parseDouble(strings.get(2)));
            stockHistoryPricePO.setLowPx(Double.parseDouble(strings.get(3)));
            stockHistoryPricePO.setClosePx(Double.parseDouble(strings.get(4)));
            stockHistoryPricePO.setBusinessAmount(Long.parseLong(strings.get(5)));
            list.add(stockHistoryPricePO);
        }
        return list;
    }

    public boolean refresh(String cookie) {
        List<StockSubjectPO> stockSubjects = stockSubjectService.findAllByType(StockSubjectEnums.Type.DAILY_PRICE);
        List<String> codes = stockSubjects.stream().map(StockSubjectPO::getCode).distinct().toList();
        List<StockPO> stockList = stockService.findAllByCodes(codes);
        for (StockPO stock : stockList) {
            try {
                int s = refreshOne(stock, cookie, 0);
            } catch (Exception e) {
                log.error("error when refresh stock history price ", e);
            }
        }

        return true;
    }

    public boolean refreshOneFully(String code, String cookie) {
        StockPO stock = stockService.findByCode(code);
        return refreshOne(stock, cookie, -2000) > 0;
    }

    public int refreshOne(StockPO stockPO, String cookie) {
        int s = refreshOne(stockPO, cookie, 0);
        return s;
    }

    public int refreshOne(StockPO stockPO, String cookie, int days) {
//        int s = refreshOneByCs(stockPO);
        int s = refreshOneByXueQiu(stockPO, cookie, days);
        return s;
    }

    public String computeOffsetDistribution(String code) {
        List<StockHistoryPricePO> priceList = this.findAllByCode(code);
        final List<StockHistoryPricePO> sortedList = priceList.stream()
            .sorted(Comparator.comparing(StockHistoryPricePO::getMinTime))
            .toList();

        ToDoubleFunction<StockHistoryPricePO> highPxFun = StockHistoryPricePO::getHighPx;
        ToDoubleFunction<StockHistoryPricePO> lowPxFun = StockHistoryPricePO::getLowPx;
        Map<String, ToDoubleFunction<StockHistoryPricePO>> map = Map.of(
            "high", highPxFun,
            "low", lowPxFun
        );
        List<String> list = map.entrySet().stream()
            .map(e -> computeOffsetDistribution(sortedList, e.getValue(), e.getKey()))
            .toList();
        return "股票日内最高价/最低价与开盘价的差值分布：" + code + " \n" + StringUtils.join(list, "\n");
    }

    private String computeOffsetDistribution(List<StockHistoryPricePO> sortedList,
                                             ToDoubleFunction<StockHistoryPricePO> fun,
                                             String title) {
        List<StockBiasOffset> stockBiasOffsetListForHigh = sortedList.stream()
            .map(e -> {
                StockBiasOffset stockBiasOffset = new StockBiasOffset();
                stockBiasOffset.setValue(fun.applyAsDouble(e));
                stockBiasOffset.setAvg(e.getOpenPx());
                stockBiasOffset.computeOffset();
                return stockBiasOffset;
            })
            .toList();
        List<Double> offsetList = stockBiasOffsetListForHigh.stream()
            .map(StockBiasOffset::getOffset)
            .toList();

        DescriptiveStatistics stats = new DescriptiveStatistics();
        Frequency frequency = new Frequency();

        for (Double v : offsetList) {
            stats.addValue(v);
            frequency.addValue(v);
        }
        double max = stats.getMax();
        double min = stats.getMin();
        // 平均值
        double mean = stats.getMean();
        // 方差：指在一组数据中，各个数据与平均数的差的平方和平均数，方差主要用于衡量一组数据的离散程度。
        double variance = stats.getVariance();
        // 标准差：又称均方差，它是方差的算术平方根。标准差和方差一样，用于表示一组数据的离散程度。
        double standardDeviation = stats.getStandardDeviation();
        // 中位数 50分位数/50点位数
        double median = stats.getPercentile(50);
        // 黄金分隔 golden ratio
        double goldenRatio = stats.getPercentile(61.8);

        return "---" + title + "---"
            + "\nmax=" + max + ", min=" + min + ", median=" + median
            + ", goldenRatio=" + goldenRatio
            + "\nmean=" + mean + ", variance=" + variance
            + ", standardDeviation=" + standardDeviation
            + "\nfrequency=\n" + frequency
            + "\n--end--";
    }

    public List<StockHistoryPricePO> findAllByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return new ArrayList<>(0);
        }
        return getRepository().findAllByCode(code);
    }

    // 需要cookie
    public int refreshOneByXueQiu(StockPO stockPO, String cookie, int days) {
        List<StockHistoryPricePO> list = getKlineDataByXq(stockPO, cookie, days);
        return saveList(list, stockPO, StockHistoryPriceEnums.Channel.XUEQIU);
    }

    List<StockHistoryPricePO> getKlineDataByXq(StockPO stockPO, final String cookie, int days) {
        String region = StockUtils.getRegion(StockEnums.Region.valueOf(stockPO.getRegion().name()));
        if (StringUtils.isBlank(region)) {
            log.debug("no region configured with stock: {}", stockPO.getCode());
            return null;
        }
        if ("SS".equals(region)) {
            region = "SH";
        }
        String code = stockPO.getCode();

        String cookieToUse = cookie;
        if (StringUtils.isBlank(cookie)) {
            cookieToUse = cookieCacheComponentWrapper.pullCookie(PushCookieReq.DOMAIN_XUEQIU);
            // 没有cookie，就没有请求的必要了
            if (StringUtils.isBlank(cookieToUse)) {
                return null;
            }
        }
        int daysToUse = days >= 0 ? -240 : days;
        XueQiuKlineResp klineResp = XueQiuStockClientUtils.kline(region + code, daysToUse, cookieToUse);
        XueQiuKlineData data = klineResp.getData();
        return deal(data, region);
    }

    // 页面已404
    // http://stockdata.cs.com.cn/qcenter/new/stock-exponent.html?en_prod_code=000001.SS
    public int refreshOneByCs(StockPO stockPO) {
        List<StockHistoryPricePO> list = getKlineDataByCs(stockPO);
        return saveList(list, stockPO, StockHistoryPriceEnums.Channel.CS);
    }

    private int saveList(List<StockHistoryPricePO> stockHistoryPriceList,
                         StockPO stockPO,
                         StockHistoryPriceEnums.Channel channelEnum) {
        if (CollectionUtils.isEmpty(stockHistoryPriceList)) {
            return 0;
        }
        String code = stockPO.getCode();
        String region = StockUtils.getRegion(StockEnums.Region.valueOf(stockPO.getRegion().name()));

        int updatedCounts = 0;
        for (StockHistoryPricePO pricePO : stockHistoryPriceList) {
            StockHistoryPriceEnums.Channel channel = pricePO.getChannel();
            if (Objects.isNull(channel)) {
                pricePO.setChannel(channelEnum);
            }
            pricePO.setCode(code);
            pricePO.setRegion(region);
            pricePO.setCreateUserId(1L);
            pricePO.setUpdateUserId(pricePO.getCreateUserId());

            StockHistoryPricePO old = getRepository().findByCodeAndRegionAndMinTime(
                pricePO.getCode(), pricePO.getRegion(), pricePO.getMinTime());
            if (Objects.isNull(old)) {
                getRepository().persist(pricePO);
                log.info("code was saved just now. code={},minTime={}",
                    pricePO.getCode(), pricePO.getMinTime());
                updatedCounts++;
            } else {
                log.info("code was saved before. code={},minTime={}",
                    pricePO.getCode(), pricePO.getMinTime());
            }
        }

        return updatedCounts;
    }

    private List<StockHistoryPricePO> getKlineDataByCs(StockPO stockPO) {
        throw new CommonException("not support");
    }

}
