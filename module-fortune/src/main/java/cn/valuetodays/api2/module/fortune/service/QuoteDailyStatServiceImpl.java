package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.QuoteDAO;
import cn.valuetodays.api2.module.fortune.dao.QuoteDailyStatDAO;
import cn.valuetodays.api2.module.fortune.persist.EtfInfoPO;
import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.valuetodays.api2.module.fortune.reqresp.AShareDailyTurnAmount;
import cn.valuetodays.api2.module.fortune.reqresp.AShareHistoryTurnAmountResp;
import cn.valuetodays.api2.module.fortune.reqresp.AShareLatestTurnAmountResp;
import cn.valuetodays.api2.module.fortune.reqresp.CodedQuoteStatVo;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatReq;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteStatGroupResp;
import cn.valuetodays.api2.module.fortune.service.kits.IndexKeyPointComputer;
import cn.valuetodays.api2.module.fortune.service.module.EastMoneyIndexModule;
import cn.valuetodays.api2.module.fortune.service.module.QuoteStatModule;
import cn.valuetodays.api2.web.common.NotifyService;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.exception.AssertUtils;
import cn.vt.rest.third.eastmoney.QuoteEnums;
import cn.vt.rest.third.utils.NumberUtils;
import cn.vt.util.DateUtils;
import cn.vt.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 指数每日统计
 *
 * @author lei.liu
 * @since 2023-04-02 13:02
 */
@ApplicationScoped
@Slf4j
public class QuoteDailyStatServiceImpl
    extends BaseCrudService<Long, QuoteDailyStatPO, QuoteDailyStatDAO> {

    public static final Function<String, QuoteDailyStatPO> TRANSFORMER = str -> {
        String[] dataArr = StringUtils.split(str, ",");
        String dateStr = dataArr[0]; // yyyy-MM-dd
        String openPxStr = dataArr[1];  // 开盘点
        String closePxStr = dataArr[2];  // 收盘点
        String highPxStr = dataArr[3];  // 最高点
        String lowPxStr = dataArr[4];  // 最低点
        String busiAmountStr = dataArr[5];  // 成交量
        String busiMoneyStr = dataArr[6];  // 成交额
        String zhenfuStr = dataArr[7];  // 振幅
        String offsetPxPercentageStr = dataArr[8];  // 涨跌幅
        String offsetPxStr = dataArr[9];  // 涨跌额
        String huanshoulvStr = dataArr[10];  // 换手率
        QuoteDailyStatPO idsp = new QuoteDailyStatPO();
        idsp.setStatDate(Integer.parseInt(StringUtils.replace(dateStr, "-", "")));
        idsp.setClosePx(Double.parseDouble(closePxStr));
        idsp.setHighPx(Double.parseDouble(highPxStr));
        idsp.setLowPx(Double.parseDouble(lowPxStr));
        idsp.setOpenPx(Double.parseDouble(openPxStr));
        idsp.setBusiAmount(new BigDecimal(busiAmountStr).longValue());
        idsp.setBusiMoney(new BigDecimal(busiMoneyStr).longValue());
        idsp.setOffsetPx(Double.parseDouble(offsetPxStr));
        idsp.setOffsetPxPercentage(Double.parseDouble(offsetPxPercentageStr));
        idsp.setZhenfuPercentage(Double.parseDouble(zhenfuStr));
        idsp.setHuanshoulvPercentage(Double.parseDouble(huanshoulvStr));
        return idsp;
    };
    private static final String cacheKey = "cache_" + QuoteDailyStatPO.TABLE_NAME;
    private static final Function<AShareDailyTurnAmount, AShareHistoryTurnAmountResp.DailyAmountData> FUN_CONVERT = req -> {
        AShareHistoryTurnAmountResp.DailyAmountData resp = new AShareHistoryTurnAmountResp.DailyAmountData();
        resp.setTurnAmount(req.getTurnAmount());
        resp.setMinTime(req.getMinTime().toString());
        return resp;
    };
    @Inject
    RedisDataSource stringRedisTemplate;
    @Inject
    EastMoneyIndexModule eastMoneyIndexModule;
    @Inject
    private EtfInfoServiceImpl etfInfoService;
    @Inject
    private QuoteDAO quoteDAO;
    @Inject
    private QuoteStatModule quoteStatModule;
    @Inject
    private NotifyService notifyService;

    public AShareLatestTurnAmountResp aShareLatestTurnAmount() {
        AShareLatestTurnAmountResp resp = new AShareLatestTurnAmountResp();
        resp.setRealtime(eastMoneyIndexModule.realtimeIndex());
        resp.setList(computeLatestTurnAmount());
        return resp;
    }

    public List<AShareLatestTurnAmountResp.SingleRecord> computeLatestTurnAmount() {
        List<QuoteDailyStatPO> list;
        String cacheStr = stringRedisTemplate.value(String.class).get(cacheKey);
        if (StringUtils.isNotBlank(cacheStr)) {
            list = JsonUtils.fromJson(cacheStr, new TypeReference<>() {
            });
        } else {
            Integer baseStatDate = DateUtils.formatAsYyyyMMdd(LocalDate.now().minusDays(31));
            list = getRepository().findByCodeInAndStatDateGreaterThan(
                Arrays.asList("000001", "399001", "899050"),
                baseStatDate
            );
            SetArgs setArgs = new SetArgs().ex(Duration.ofMinutes(10));
            stringRedisTemplate.value(String.class).set(cacheKey, JsonUtils.toJson(list), setArgs);
        }

        Map<Integer, List<QuoteDailyStatPO>> statDateMaps = list.stream()
            .collect(Collectors.groupingBy(QuoteDailyStatPO::getStatDate));

        Comparator<AShareLatestTurnAmountResp.SingleRecord> comparator =
            Comparator.comparingInt(AShareLatestTurnAmountResp.SingleRecord::getMinTime).reversed();

        return statDateMaps.entrySet().stream()
            .map(e -> {
                Integer key = e.getKey();
                List<QuoteDailyStatPO> value = e.getValue();
                return buildSingleRecord(key, value);
            })
            .sorted(comparator)
            .toList();
    }

    private AShareLatestTurnAmountResp.SingleRecord buildSingleRecord(Integer key, List<QuoteDailyStatPO> value) {
        AShareLatestTurnAmountResp.SingleRecord sr = new AShareLatestTurnAmountResp.SingleRecord();
        sr.setMinTime(key);
        QuoteDailyStatPO shPO = findMatchedValue(value, QuoteEnums.Region.SHANGHAI);
        sr.setSh(Optional.ofNullable(shPO).map(QuoteDailyStatPO::getBusiMoney).orElse(0L));
        sr.setShTotalMarketCapByWanYi(NumberUtils.computeByWanYi(BigDecimal.valueOf(computeTotalMarketCap(shPO))).doubleValue());
        QuoteDailyStatPO szPO = findMatchedValue(value, QuoteEnums.Region.SHENZHEN);
        sr.setSz(Optional.ofNullable(szPO).map(QuoteDailyStatPO::getBusiMoney).orElse(0L));
        sr.setSzTotalMarketCapByWanYi(NumberUtils.computeByWanYi(BigDecimal.valueOf(computeTotalMarketCap(szPO))).doubleValue());
        QuoteDailyStatPO bzPO = findMatchedValue(value, QuoteEnums.Region.NEEQ);
        sr.setBz50(Optional.ofNullable(bzPO).map(QuoteDailyStatPO::getBusiMoney).orElse(0L));
        sr.setBzTotalMarketCapByWanYi(NumberUtils.computeByWanYi(BigDecimal.valueOf(computeTotalMarketCap(bzPO))).doubleValue());
        sr.setTotalAShare(
            sr.getSh() + sr.getSz()
        );
        return sr;
    }

    private Double computeTotalMarketCap(QuoteDailyStatPO shPO) {
        if (Objects.isNull(shPO)) {
            return 0D;
        }
        Long busiMoney = shPO.getBusiMoney();
        Double huanshoulvPercentage = shPO.getHuanshoulvPercentage();
        if (busiMoney == 0L) {
            return 0D;
        }
        return busiMoney * 100.0 / huanshoulvPercentage;
    }

    private QuoteDailyStatPO findMatchedValue(List<QuoteDailyStatPO> values, QuoteEnums.Region region) {
        return values.stream()
            .filter(v -> Objects.equals(v.getRegion(), region.name()))
            .findFirst().orElse(null);
    }

    /**
     * 每日刷新指数的当天统计数据
     */
    public boolean refreshAll() {
        List<QuotePO> all = quoteDAO.listAll();
        for (QuotePO quotePO : all) {
            try {
                refreshOne(quotePO);
            } catch (Exception e) {
                log.error("error when refreshOne()", e);
            }
        }
        stringRedisTemplate.key(String.class).del(cacheKey);
        notifyAShareDailyTurnAmount0();
        return true;
    }

    private void notifyAShareDailyTurnAmount0() {
        LocalDateTime today = DateUtils.getToday();
        int yyyyMMdd = Integer.parseInt(DateUtils.formatDate(today).replace("-", ""));
        AShareLatestTurnAmountResp.SingleRecord preTradeTurnAmount = getPreTradeTurnAmount(today);
        String content = eastMoneyIndexModule.buildAShareRealtimeTurnAmountMsg(yyyyMMdd, preTradeTurnAmount);
        notifyService.notifyAShareDailyTurnAmount(yyyyMMdd, content);
    }

    private AShareLatestTurnAmountResp.SingleRecord getPreTradeTurnAmount(LocalDateTime dateTime) {
        LocalDateTime tmpDateTime = dateTime;
        while (true) {
            tmpDateTime = tmpDateTime.minusDays(1);
            int yyyyMMdd = Integer.parseInt(DateUtils.formatDate(tmpDateTime).replace("-", ""));
            List<QuoteDailyStatPO> list = getRepository().findByCodeInAndStatDate(
                Arrays.asList("000001", "399001", "899050"),
                yyyyMMdd
            );
            if (CollectionUtils.isNotEmpty(list)) {
                return buildSingleRecord(yyyyMMdd, list);
            }
        }
    }

    public AShareHistoryTurnAmountResp aShareHistoryTurnAmount() {
        List<AShareDailyTurnAmount> aShareDailyTurnAmounts = getRepository().findAShareDailyTurnAmount();
        List<AShareHistoryTurnAmountResp.DailyAmountData> dataList = aShareDailyTurnAmounts.stream()
            .filter(Objects::nonNull)
            .map(FUN_CONVERT)
            .toList();
        AShareHistoryTurnAmountResp resp = new AShareHistoryTurnAmountResp();
        resp.setList(dataList);
        return resp;
    }

    public List<QuoteDailyStatPO> computeMonthlySuggest(String code) {
        List<QuoteDailyStatPO> list = this.findAllByCodeOrderByMinTimeDesc(code);
        QuoteDailyStatPO last = list.get(0);
        final int size = list.size();
        QuoteDailyStatPO first = list.get(size - 1);
        log.info("first={}", first);
        log.info("last={}", last);
        log.info("size={}", size);
        // 只处理发行超过4年的数据
        if (size < 250 * 4) {
            return new ArrayList<>(0);
        }

        return getRepository().findAllByCodeOrderByStatDateDesc(code);
    }

    public List<QuoteDailyStatPO> findAllByCodeOrderByMinTimeDesc(String code) {
        return getRepository().findAllByCodeOrderByStatDateDesc(code);
    }

    public List<QuoteDailyStatPO> computeKeyPointsByCode(String code) {
        // 涨跌幅10%以内数据都舍弃
        double offsetPercentage = 0.1;

        List<QuoteDailyStatPO> list = this.findAllByCodeOrderByMinTimeDesc(code);
        IndexKeyPointComputer c = new IndexKeyPointComputer();
        return c.doCompute(list, offsetPercentage);
    }

    public boolean refreshOne(QuotePO quotePO) {
        int n = eastMoneyIndexModule.refreshOne0(quotePO, false);
        log.info("refreshOne process {} records", n);
        return true;
    }

    public boolean refreshOneFully(QuotePO quotePO) {
        int n = eastMoneyIndexModule.refreshOne0(quotePO, true);
        log.info("refreshOne process {} records", n);
        return true;
    }

    /**
     * @param code  code
     * @param fully 是否全量更新
     * @return 更新的数据的条数
     */
    public int refreshOne(String code, boolean fully) {
        AssertUtils.assertStringNotBlank(code, "code不能为空");
        QuotePO old = quoteDAO.findByCode(code);
        AssertUtils.assertNotNull(old, "code对应的fortune_quote记录不存在");
        return eastMoneyIndexModule.refreshOne0(old, fully);
    }

    public QuoteStatGroupResp computeStats(String code) {
        List<QuoteDailyStatPO> list = getRepository().findAllByCodeOrderByStatDateDesc(code);
        return quoteStatModule.computeStatsForToday(list);
    }

    public List<CodedQuoteStatVo> computeStatsForAllCodes() {
        List<EtfInfoPO> allEtfs = etfInfoService.list();
        Map<String, EtfInfoPO> codeAndEtfInfoMap = allEtfs.stream()
            .collect(Collectors.toMap(EtfInfoPO::getCode, e -> e));
        List<QuotePO> allIndex = quoteDAO.listAll();
        List<CodedQuoteStatVo> allCodeStats = new ArrayList<>(allIndex.size());
        for (QuotePO index : allIndex) {
            String code = index.getCode();
            QuoteStatGroupResp groupResp = this.computeStats(code);
            CodedQuoteStatVo one = new CodedQuoteStatVo();
            one.setCode(code);
            one.setName(index.getName());
            one.setGroupResp(groupResp);
            one.setSuggestEtfs(parseSuggestEtfs(codeAndEtfInfoMap, index.getSuggestEtfs()));
            allCodeStats.add(one);
        }
        return allCodeStats;
    }

    private List<EtfInfoPO> parseSuggestEtfs(Map<String, EtfInfoPO> codeAndEtfInfoMap, String suggestEtfsStr) {
        String trimmed = StringUtils.trim(suggestEtfsStr);
        if (StringUtils.isBlank(trimmed)) {
            return new ArrayList<>(0);
        }
        String replaced = trimmed.replace("，", ",");
        String[] splits = StringUtils.split(replaced, ",");
        return Arrays.stream(splits)
            .map(codeAndEtfInfoMap::get)
            .filter(Objects::nonNull)
            .toList();
    }

    public DailyOffsetStatResp computeDailyOffsetStat(DailyOffsetStatReq req) {
        String code = req.getCode();
        List<QuoteDailyStatPO> list = getRepository().findAllByCodeOrderByStatDateDesc(code);
        return quoteStatModule.computeDailyOffsetStat(list);
    }

}
