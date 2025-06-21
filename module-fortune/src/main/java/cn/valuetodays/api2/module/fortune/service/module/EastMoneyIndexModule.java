package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.QuoteDailyStatDAO;
import cn.valuetodays.api2.module.fortune.enums.QuoteDailyStatEnums;
import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.valuetodays.api2.module.fortune.reqresp.AShareLatestTurnAmountResp;
import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import cn.vt.rest.third.eastmoney.QuoteEnums;
import cn.vt.rest.third.eastmoney.vo.EastMoneyRealtimeIndexData;
import cn.vt.rest.third.eastmoney.vo.EastMoneyRealtimeIndexResp;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockKlineData;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockKlineResp;
import cn.vt.rest.third.utils.NumberUtils;
import cn.vt.rest.third.xueqiu.StockRealtimeQuoteComponent;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteData;
import cn.vt.util.DateUtils;
import cn.vt.util.HttpClient4Utils;
import cn.vt.util.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-27
 */
@ApplicationScoped
@Slf4j
public class EastMoneyIndexModule {
    @Inject
    QuoteDailyStatDAO quoteDailyStatDAO;

    public AShareLatestTurnAmountResp.SingleRecord realtimeIndex() {
        AShareLatestTurnAmountResp.SingleRecord sr = new AShareLatestTurnAmountResp.SingleRecord();
        realtimeIndexPart1(sr);
        realtimeIndexPart2(sr);
        return sr;
    }

    public String buildAShareRealtimeTurnAmountMsg(int yyyyMMdd,
                                                   AShareLatestTurnAmountResp.SingleRecord preTradeTurnAmount) {
        AShareLatestTurnAmountResp.SingleRecord sr = this.realtimeIndex();
        Double totalAShareByYi = sr.getTotalAShareByYi();
        Double shByYi = sr.getShByYi();
        Double szByYi = sr.getSzByYi();

        String msg = "A股今日" + yyyyMMdd + "成交额统计："
            + "总额" + totalAShareByYi.intValue() + "亿，"
            + "沪市" + shByYi.intValue() + "亿，"
            + "深市" + szByYi.intValue() + "亿";

        if (Objects.nonNull(preTradeTurnAmount)) {
            Double preTotalByYi = preTradeTurnAmount.getTotalAShareByYi();
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(0);
            nf.setRoundingMode(RoundingMode.UP);
            nf.setGroupingUsed(false); // 关闭分组，显示将不再以千位符分隔
            String offsetStr = nf.format(totalAShareByYi - preTotalByYi);
            msg += "；比上交易日" + (offsetStr) + "亿";
        }

        return msg;
    }

    public void realtimeIndexPart2(AShareLatestTurnAmountResp.SingleRecord sr) {
        QuoteEnums.Region shanghai = QuoteEnums.Region.SHANGHAI;
        QuoteEnums.Region shenzhen = QuoteEnums.Region.SHENZHEN;
        QuoteEnums.Region neeq = QuoteEnums.Region.NEEQ;
        List<XueQiuStockRealtimeQuoteData> list = StockRealtimeQuoteComponent.doGet(
            List.of(
                shanghai.formatPrefixIndexCodeUpper(),
                shenzhen.formatPrefixIndexCodeUpper(),
                neeq.formatPrefixIndexCodeUpper()
            )
        );
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, XueQiuStockRealtimeQuoteData> codeAndObjMap = list.stream()
            .collect(Collectors.toMap(XueQiuStockRealtimeQuoteData::getSymbol, e -> e));
        sr.setShTotalMarketCapByWanYi(findMatchedMarketCapital(codeAndObjMap, shanghai));
        sr.setSzTotalMarketCapByWanYi(findMatchedMarketCapital(codeAndObjMap, shenzhen));
        sr.setBzTotalMarketCapByWanYi(findMatchedMarketCapital(codeAndObjMap, neeq));
    }

    private Double findMatchedMarketCapital(Map<String, XueQiuStockRealtimeQuoteData> codeAndObjMap,
                                            QuoteEnums.Region region) {
        XueQiuStockRealtimeQuoteData obj = codeAndObjMap.get(region.formatPrefixIndexCodeUpper());
        if (Objects.isNull(obj)) {
            return 0D;
        }
        return NumberUtils.computeByWanYi(BigDecimal.valueOf(obj.getMarketCapital())).doubleValue();
    }

    public void realtimeIndexPart1(AShareLatestTurnAmountResp.SingleRecord sr) {
        String url = "http://push2.eastmoney.com/api/qt/ulist/get"
            + "?"
            + "fltt=1"
            + "&invt=2"
            + "&cb=jQuery351007312525601113506_" + System.currentTimeMillis()
            + "&fields=f12%2Cf13%2Cf14%2Cf1%2Cf2%2Cf4%2Cf3%2Cf152%2Cf6%2Cf104%2Cf105%2Cf106"
            + "&secids=1.000001%2C0.399001%2C0.899050"
            + "&ut=fa5fd1943c7b386f172d6893dbfba10b"
            + "&pn=1"
            + "&np=1"
            + "&pz=20"
            + "&wbp2u=%7C0%7C0%7C0%7Cweb"
            + "&_=" + System.currentTimeMillis();
        String respStr = HttpClient4Utils.doGet(url);
        int inx = respStr.indexOf("({");
        int endInx = respStr.indexOf("});", inx);
        String substring = respStr.substring(inx + "({".length(), endInx);
        String jsonStr = "{" + substring + "}";
        EastMoneyRealtimeIndexResp resp = JsonUtils.fromJson(jsonStr, EastMoneyRealtimeIndexResp.class);
        EastMoneyRealtimeIndexData data = resp.getData();
        List<EastMoneyRealtimeIndexData.Diff> diff = data.getDiff();
        sr.setMinTime(-1);
        sr.setSh(findMatchedValue(diff, QuoteEnums.Region.SHANGHAI));
        sr.setSz(findMatchedValue(diff, QuoteEnums.Region.SHENZHEN));
        sr.setTotalAShare(
            sr.getSh() + sr.getSz()
        );
        sr.setBz50(findMatchedValue(diff, QuoteEnums.Region.NEEQ));
    }

    // 舍弃小数点
    private Long findMatchedValue(List<EastMoneyRealtimeIndexData.Diff> values, QuoteEnums.Region region) {
        return values.stream()
            .filter(v -> Objects.equals(region.indexCode(), v.getCode()))
            .findFirst()
            .map(e -> {
                String busiMoney = e.getBusiMoney();
                if (StringUtils.isBlank(busiMoney) || StringUtils.equals(busiMoney, "-")) {
                    return 0L;
                }
                return Optional.of(org.apache.commons.lang3.math.NumberUtils.createDouble(busiMoney))
                    .map(Double::longValue)
                    .orElse(0L);
            }).orElse(0L);
    }

    public int refreshOne0(QuotePO quotePO, boolean fully) {
        String code = quotePO.getCode();
        QuoteEnums.Region region = quotePO.getRegion();
        // page : https://quote.eastmoney.com/zs000300.html
        // 处理全量数据的天数要大一点，使用120处理增量数据
        long lmt = 120;
        if (fully) {
            lmt = DateUtils.intervalDays(LocalDate.of(1979, 1, 1));
        }
        String url = "http://27.push2his.eastmoney.com/api/qt/stock/kline/get?"
            + "cb=jQuery35105866457293913834_1680408774022"
//            + "&secid=1.000001"
            + "&secid=" + region.secid() + "." + code
            + "&ut=fa5fd1943c7b386f172d6893dbfba10b"
            + "&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6"
            + "&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61"
            + "&klt=101"
            + "&fqt=1"
            // ?为什么是这个值
            + "&end=20500101"
            + "&lmt=" + lmt
//            + "&_=1680408774027"
            + "&_=" + System.currentTimeMillis();

        String respStr = HttpClient4Utils.doGet(url);
        int inx = respStr.indexOf("({");
        int endInx = respStr.indexOf("});", inx);
        String substring = respStr.substring(inx + "({".length(), endInx);
        String jsonStr = "{" + substring + "}";
        EastMoneyStockKlineResp klineResp = JsonUtils.fromJson(jsonStr, EastMoneyStockKlineResp.class);
        EastMoneyStockKlineData data = klineResp.getData();
        if (Objects.isNull(data)) {
            return 0;
        }
        String[] klines = data.getKlines();
        log.info("klines: {}", klines.length);
        List<QuoteDailyStatPO> list = Arrays.stream(klines)
            .map(QuoteDailyStatServiceImpl.TRANSFORMER)
            .peek(e -> {
                e.setCode(code);
                e.setRegion(region.name());
                e.setChannel(QuoteDailyStatEnums.Channel.EASTMONEY);
            })
            .toList();

        int n = 0;
        for (QuoteDailyStatPO toSave : list) {
            QuoteDailyStatPO old = quoteDailyStatDAO.findByCodeAndStatDate(toSave.getCode(), toSave.getStatDate());
            if (Objects.nonNull(old)) {
                old.setClosePx(toSave.getClosePx());
                old.setHighPx(toSave.getHighPx());
                old.setLowPx(toSave.getLowPx());
                old.setOpenPx(toSave.getOpenPx());
                old.setBusiAmount(toSave.getBusiAmount());
                old.setBusiMoney(toSave.getBusiMoney());
                old.setOffsetPx(toSave.getOffsetPx());
                old.setOffsetPxPercentage(toSave.getOffsetPxPercentage());
                old.setZhenfuPercentage(toSave.getZhenfuPercentage());
                old.setHuanshoulvPercentage(toSave.getHuanshoulvPercentage());
            } else {
                old = toSave;
            }
            try {
                quoteDailyStatDAO.persist(old);
                n++;
            } catch (Exception e) {
                log.error("error when save {}", toSave, e);
            }
        }
        return n;
    }

}
