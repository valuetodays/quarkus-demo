package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.EtfT0DailyInfoDAO;
import cn.valuetodays.api2.module.fortune.persist.EtfT0DailyInfoPersist;
import cn.valuetodays.api2.module.fortune.persist.EtfT0PO;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import cn.vt.rest.third.eastmoney.EastMoneyStockModule;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailDataTyped;
import cn.vt.rest.third.jisilu.JisiluEtfClientUtils;
import cn.vt.rest.third.jisilu.vo.EtfHistoryDetailsResp;
import cn.vt.rest.third.sse.SseEtfClientUtils;
import cn.vt.rest.third.sse.vo.EtfDailyVolumeResp;
import cn.vt.rest.third.sse.vo.TotalSharesResp;
import cn.vt.rest.third.utils.NumberUtils;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-06
 */
@ApplicationScoped
@Slf4j
public class EtfT0DailyInfoModule {
    @Inject
    SqlServiceImpl sqlService;

    @Inject
    EtfT0DailyInfoDAO etfT0DailyInfoDAO;

    @Transactional
    public int refreshOne(EtfT0PO etfT0) {
        String code = etfT0.getCode();
        EastMoneyStockDetailDataTyped stockDetail = EastMoneyStockModule.getStockDetail(code);
        LocalDateTime statDateTime = stockDetail.getStatDateTime();
        EtfT0DailyInfoPersist old = etfT0DailyInfoDAO.findByCodeAndStatDate(code, DateUtils.formatAsYyyyMMdd(statDateTime.toLocalDate()));
        if (Objects.isNull(old)) {
            old = new EtfT0DailyInfoPersist();
        }
        old.setCode(stockDetail.getCode());
        BigDecimal tradeVolumeWan = NumberUtils.computeByWan(stockDetail.getTradeVolumeInShou().multiply(BigDecimal.valueOf(100)));
        old.setTradeVolumeWanIfAbsent(tradeVolumeWan);
        old.setTradeAmountWan(NumberUtils.computeByWan(stockDetail.getTradeAmount()));
        old.setHuanShouPtg(stockDetail.getHuanShouPtg());
        old.setOpenPx(stockDetail.getOpenPrice());
        old.setClosePx(stockDetail.getPrice());
        old.setHighPx(stockDetail.getHighestPrice());
        old.setLowPx(stockDetail.getLowestPrice());
        old.setInnerPanWan(NumberUtils.computeByWan(stockDetail.getInPan()));
        old.setOuterPanWan(NumberUtils.computeByWan(stockDetail.getOutPan()));
        old.setLiangbi(stockDetail.getLiangbi());
        final LocalDate localDate = stockDetail.getStatDateTime().toLocalDate();
        old.setStatDate(DateUtils.formatAsYyyyMMdd(localDate));
        old.setYiJiaPtgIfAbsent(EtfT0DailyInfoPersist.DEFAULT_YI_JIA_PTG);
        old.setTotalSharesWanIfAbsent(EtfT0DailyInfoPersist.DEFAULT_TOTAL_SHARES_WAN);

        etfT0DailyInfoDAO.persist(old);
        return 1;
    }

    public void fillTotalSharesWan(LocalDate localDate) {
        List<EtfDailyVolumeResp.Item> dailyVolumes = SseEtfClientUtils.getDailyVolumes(localDate);
        if (CollectionUtils.isEmpty(dailyVolumes)) {
            return;
        }
        final String sqlUpdate = "update fortune_etf_t0_daily_info set total_shares_wan=? "
            + " where code=? and stat_date=?";
        List<Object[]> batchArgs = dailyVolumes.stream()
            .map(e -> {
                String code = e.getCode();
                String statDateStr = e.getStatDateStr();
                BigDecimal totVolumeInWan = e.getTotVolume();
                if (Objects.isNull(totVolumeInWan)) {
                    return null;
                }
                int statDate = Integer.parseInt(StringUtils.replace(statDateStr, "-", ""));
                return new Object[]{totVolumeInWan, code, statDate};
            })
            .filter(Objects::nonNull)
            .toList();
        int[] ints = sqlService.batchUpdate(sqlUpdate, batchArgs);
        int length = ArrayUtils.getLength(ints);
        log.info("update {} records for {}", length, localDate);
    }

    private void fillTotalSharesWan(LocalDate localDate, EtfT0DailyInfoPersist obj) {
        String code = obj.getCode();
        try {
            TotalSharesResp totalSharesResp = SseEtfClientUtils.getTotalVolumeInWan(code, localDate);
            List<TotalSharesResp.Item> result = totalSharesResp.getResult();
            if (CollectionUtils.isNotEmpty(result)) {
                TotalSharesResp.Item item = result.getFirst();
                BigDecimal totalVolumeInWan = item.getTotalVolumeInWan();
                if (Objects.nonNull(totalVolumeInWan)) {
                    obj.setTotalSharesWan(totalVolumeInWan);
                }
            }
        } catch (Exception e) {
            log.error("error when fillTotalSharesWan() for code {}", code, e);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void fixTotalSharesByCode(String code) {
        EtfHistoryDetailsResp resp = JisiluEtfClientUtils.getHistoryDetails(code);
        if (Objects.isNull(resp)) {
            return;
        }
        List<EtfHistoryDetailsResp.Row> rows = resp.getRows();
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        final String sqlUpdate = "update fortune_etf_t0_daily_info set total_shares_wan=?, yi_jia_ptg=? "
            + " where code=? and stat_date=?";
        List<Object[]> batchArgs = rows.stream()
            .map(e -> {
                EtfHistoryDetailsResp.Cell cell = e.getCell();
                BigDecimal totalVolumeInWan = cell.getTotalVolumeInWan();
                // 去掉不合理数据
                if (Objects.isNull(totalVolumeInWan) || BigDecimal.ZERO.compareTo(totalVolumeInWan) >= 0) {
                    return null;
                }
                BigDecimal discountRt = cell.getDiscount_rt();
                int statDate = Integer.parseInt(StringUtils.replace(cell.getPrice_dt(), "-", ""));
                return new Object[]{totalVolumeInWan, discountRt, code, statDate};
            })
            .filter(Objects::nonNull)
            .toList();
        int[] ints = sqlService.batchUpdate(sqlUpdate, batchArgs);
        int length = ArrayUtils.getLength(ints);
        log.info("update {} records for {}", length, code);
    }
}
