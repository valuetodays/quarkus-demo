package cn.valuetodays.api2.module.fortune.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.StockMinutePriceDAO;
import cn.valuetodays.api2.module.fortune.enums.StockHistoryPriceEnums;
import cn.valuetodays.api2.module.fortune.enums.StockSubjectEnums;
import cn.valuetodays.api2.module.fortune.persist.StockMinutePricePO;
import cn.valuetodays.api2.module.fortune.persist.StockSubjectPO;
import cn.valuetodays.api2.module.fortune.reqresp.StockMinutePriceFindAllByCodeReq;
import cn.valuetodays.api2.module.fortune.reqresp.offset.StockBiasOffset;
import cn.valuetodays.api2.web.ICookieCacheComponent;
import cn.valuetodays.api2.web.common.NotifyService;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.utils.StockCodeUtils;
import cn.vt.rest.third.xueqiu.XueQiuStockClientUtils;
import cn.vt.rest.third.xueqiu.vo.PushCookieReq;
import cn.vt.rest.third.xueqiu.vo.XueQiuMinuteChartData;
import cn.vt.rest.third.xueqiu.vo.XueQiuMinuteChartResp;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteData;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.hibernate.exception.ConstraintViolationException;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-24
 */
@ApplicationScoped
@Slf4j
public class StockMinutePriceServiceImpl
    extends BaseCrudService<Long, StockMinutePricePO, StockMinutePriceDAO> {
    @Inject
    StockSubjectServiceImpl stockSubjectService;
    @Inject
    private NotifyService notifyService;
    @Inject
    private ICookieCacheComponent cookieCacheComponentWrapper;


    //    @Async
    public void refreshByAsync(String cookie) {
        int n = refreshBySync(cookie);
        if (n > 0) {
            notifyService.notifyStockMinutePrice();
        }
    }

    public int refreshBySync(String cookie) {
        List<StockSubjectPO> list = stockSubjectService.findAllByType(StockSubjectEnums.Type.MINUTE_PRICE);
        List<String> codes = list.stream()
            .map(e -> StockCodeUtils.buildForXueQiu(e.getCode()))
            .toList();
        int n = 0;
        for (String code : codes) {
            try {
                int part = refreshOne(code, cookie);
                n += part;
            } catch (Exception e) {
                String message = e.getMessage();
                if (StringUtils.contains(message, "请刷新页面或者重新登录帐号后再试")) {
                    notifyService.notifyNoCookie(PushCookieReq.DOMAIN_XUEQIU);
                    // 出现一次即可返回了，其它code往下请求也是同样的异常
                    return n;
                } else {
                    log.error("refreshOne {} error", code, e);
                }
            }
        }
        return n;
    }

    public Integer biasPercentage(String codeWithRegion) {
        XueQiuStockRealtimeQuoteData realtimeQuoteData = XueQiuStockClientUtils.realtimeQuoteOne(codeWithRegion);
        if (Objects.isNull(realtimeQuoteData)) {
            return -1;
        }
        StockBiasOffset currentItem = new StockBiasOffset();
        currentItem.setValue(realtimeQuoteData.getCurrent());
        currentItem.setAvg(realtimeQuoteData.getAvgPrice());
        currentItem.computeOffset();
        currentItem.computeBias();

        List<StockMinutePricePO> list = getRepository().findAllByCodeOrderByTsDesc(codeWithRegion);
        if (CollectionUtils.isEmpty(list)) {
            return -1;
        }

        Frequency f = new Frequency();
        List<StockBiasOffset> stockBiasOffsets = list.stream()
            .map(e -> {
                StockBiasOffset stockBiasOffset = new StockBiasOffset(e.getClosePx(), e.getAvgPx());
                stockBiasOffset.computeBias();
                return stockBiasOffset;
            })
            .toList();
        for (StockBiasOffset item : stockBiasOffsets) {
            f.addValue(item.getBias());
        }
        // 计算累计百分比(小于等于某值)
        double cumPct = f.getCumPct(currentItem.getBias());
        return (int) (cumPct * 10000);
    }

    //    @Cacheable(value = "findAllByCode#7m", cacheManager = RedisCache2Configuration.CACHE2_MANAGER_NAME)
    public List<StockMinutePricePO> findAllByCode(StockMinutePriceFindAllByCodeReq req) {
        String code = req.getCode();
        return getRepository().findAllByCodeOrderByTsDesc(code);
    }

    /**
     * @param code eg. 600036
     */
    public String computeOffsetDistribution(String code) {
        StockMinutePriceFindAllByCodeReq req = new StockMinutePriceFindAllByCodeReq();
        req.setCode(code);
        List<StockMinutePricePO> priceList = this.findAllByCode(req);
        final List<StockMinutePricePO> sortedList = priceList.stream()
            .sorted(Comparator.comparing(StockMinutePricePO::getTs))
            .toList();
        List<StockBiasOffset> stockBiasOffsetList = sortedList.stream()
            .map(e -> {
                StockBiasOffset stockBiasOffset = new StockBiasOffset();
                stockBiasOffset.setValue(e.getClosePx());
                stockBiasOffset.setAvg(e.getAvgPx());
                stockBiasOffset.computeOffset();
//                stockBiasOffset.computeBias();
                return stockBiasOffset;
            })
            .toList();
        List<Double> offsetList = stockBiasOffsetList.stream()
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


        return "---均价/现在差值分布：" + code + "---"
            + "\nmax=" + max + ", min=" + min + ", median=" + median
            + ", goldenRatio=" + goldenRatio
            + "\nmean=" + mean + ", variance=" + variance
            + ", standardDeviation=" + standardDeviation
            + "\nfrequency=\n" + frequency
            + "\n--end--";
    }

    /**
     * @param codeWithRegion 示例值 SH600036
     */
    private int refreshOne(String codeWithRegion, String cookie) {
        String cookieToUse = cookie;
        if (StringUtils.isBlank(cookie)) {
            cookieToUse = cookieCacheComponentWrapper.pullCookie(PushCookieReq.DOMAIN_XUEQIU);
            // 没有cookie，就没有请求的必要了
            if (StringUtils.isBlank(cookieToUse)) {
                return 0;
            }
        }
        XueQiuMinuteChartResp resp = XueQiuStockClientUtils.minuteChart1d(codeWithRegion, cookieToUse);
        XueQiuMinuteChartData data = resp.getData();
        List<XueQiuMinuteChartData.Item> items = data.getItems();
        List<StockMinutePricePO> poList = items.stream()
            // 时间新的在前面，这样保存时先保存新数据
            .sorted(Comparator.comparingLong(XueQiuMinuteChartData.Item::getTimestamp).reversed())
            .map(e -> {
                StockMinutePricePO po = new StockMinutePricePO();
                po.setCode(StockCodeUtils.parseCode(codeWithRegion));
                po.setChannel(StockHistoryPriceEnums.Channel.XUEQIU);
                po.setTs(DateUtils.getDate(e.getTimestamp()));
                po.setAvgPx(e.getAvg_price());
                po.setClosePx(e.getCurrent());
                return po;
            })
            .toList();
        int n = 0;
        for (StockMinutePricePO po : poList) {
            try {
                save(po);
                n++;
            } catch (ConstraintViolationException ignored) {
                // 数据已重复了，就不再处理了。
                break;
            } catch (Exception e) {
                log.error("error when refreshOne({})", codeWithRegion, e);
            }
        }
        return n;
    }

}
