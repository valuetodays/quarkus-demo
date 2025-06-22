package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import cn.valuetodays.api2.module.fortune.client.enums.StockAlertEnums;
import cn.valuetodays.api2.module.fortune.client.persist.StockAlertLogPersist;
import cn.valuetodays.api2.module.fortune.client.persist.StockAlertPersist;
import cn.valuetodays.api2.module.fortune.dao.StockAlertDAO;
import cn.valuetodays.api2.module.fortune.dao.StockAlertLogDAO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.eastmoney.EastMoneyIndexUtils;
import cn.vt.rest.third.eastmoney.EastMoneyStockUtils;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailDataTyped;
import cn.vt.rest.third.eastmoney.vo.QuoteDailyStatVO;
import cn.vt.util.DateUtils;
import cn.vt.util.HttpClient4Utils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 股票告警
 *
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@ApplicationScoped
@Slf4j
public class StockAlertService
    extends BaseCrudService<Long, StockAlertPersist, StockAlertDAO> {

    @Inject
    StockAlertLogDAO stockAlertLogDAO;

    public void scheduleAlert(StockAlertEnums.ScheduleType scheduleType) {
        List<StockAlertPersist> list = getRepository().findAllByStatusAndScheduleType(StockAlertEnums.Status.NORMAL, scheduleType);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (StockAlertPersist p : list) {
            String code = p.getCode();
            StockAlertEnums.CodeType codeType = p.getCodeType();

            BigDecimal realtimePoint = BigDecimal.ZERO;
            BigDecimal changeOffsetPricePtg = BigDecimal.ZERO;
            if (StockAlertEnums.CodeType.STOCK == codeType) {
                EastMoneyStockDetailDataTyped realtimeStockDetail =
                    EastMoneyStockUtils.getRealtimeStockDetail(code);
                realtimePoint = realtimeStockDetail.getPrice();
                changeOffsetPricePtg = realtimeStockDetail.getChangeOffsetPricePtg();
            } else if (StockAlertEnums.CodeType.INDEX == codeType) {
                List<QuoteDailyStatVO> indexKlines = EastMoneyIndexUtils.getIndexKline(code);
                if (CollectionUtils.isEmpty(indexKlines)) {
                    log.error("no klines for data id: {}", p.getId());
                    continue;
                } else {
                    QuoteDailyStatVO last = indexKlines.getLast();
                    realtimePoint = BigDecimal.valueOf(last.getClosePx());
                    changeOffsetPricePtg = BigDecimal.valueOf(last.getOffsetPxPercentage());
                }
            }

            try {
                if (BigDecimal.ZERO.compareTo(p.getCurrentPoint()) >= 0) {
                    alertDailyOffsetToAppIfNecessary(p, changeOffsetPricePtg);
                } else {
                    alertMsgToAppIfNecessary(p, realtimePoint);
                }
            } catch (Exception e) {
                log.warn("error when alertMsgToAppIfNecessary()", e);
            }
        }

    }

    private void alertDailyOffsetToAppIfNecessary(StockAlertPersist p, BigDecimal changeOffsetPricePtg) {
        BigDecimal targetPtg = p.getTargetPtg();
        String code = p.getCode();
        // 跌
        if (BigDecimal.ZERO.compareTo(targetPtg) >= 0) {
            if (changeOffsetPricePtg.compareTo(targetPtg) <= 0) {
                String msg = "达到设定的跌幅啦：设定在" + DateUtils.formatDate(p.getCreateTime()) + "的" + code
                    + "设定了当天涨跌幅度为" + targetPtg + "%，已达到目标值（" + changeOffsetPricePtg + "）。";
                saveAlertLog(p, BigDecimal.valueOf(-1), changeOffsetPricePtg);
                alertMsgToApp(msg);
            }
        } else { // 涨
            if (changeOffsetPricePtg.compareTo(targetPtg) >= 0) {
                // 达到一定涨幅
                String msg = "达到设定的跌幅啦：设定在" + DateUtils.formatDate(p.getCreateTime()) + "的" + code
                    + "设定了当天涨跌幅度为" + targetPtg + "%，已达到目标值（" + changeOffsetPricePtg + "）。";
                saveAlertLog(p, BigDecimal.valueOf(-1), changeOffsetPricePtg);
                alertMsgToApp(msg);
            }
        }
    }

    private void alertMsgToAppIfNecessary(StockAlertPersist p,
                                          BigDecimal realtimePoint) {
        BigDecimal pointWhenAlert = p.getCurrentPoint();
        BigDecimal targetPoint = p.getTargetPoint();
        BigDecimal targetPtg = p.getTargetPtg();
        String code = p.getCode();
        boolean reachAlertWhenUp = (pointWhenAlert.compareTo(targetPoint) < 0 && targetPoint.compareTo(realtimePoint) <= 0);
        boolean reachAlertWhenDown = (pointWhenAlert.compareTo(targetPoint) > 0 && targetPoint.compareTo(realtimePoint) > 0);
        BigDecimal realChgPtg = realtimePoint.subtract(pointWhenAlert)
            .divide(pointWhenAlert, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        if (reachAlertWhenUp || reachAlertWhenDown) {
            // alert now
            String msg = "股票到达设定的条件啦：设定在" + DateUtils.formatDate(p.getCreateTime()) + "的" + code
                + "的点位是" + pointWhenAlert + "，目标点位是" + targetPoint
                + "，当前点位是" + realtimePoint + "，"
                + "，涨跌幅度为" + realChgPtg + "%";
            saveAlertLog(p, realtimePoint, realChgPtg);
            alertMsgToApp(msg);
        }
        if (BigDecimal.ZERO.compareTo(targetPtg) != 0) {
            targetPoint = pointWhenAlert.multiply(targetPtg);
            reachAlertWhenUp = (targetPtg.compareTo(BigDecimal.ONE) > 0 && targetPoint.compareTo(realtimePoint) <= 0);
            reachAlertWhenDown = (targetPtg.compareTo(BigDecimal.ONE) < 0 && targetPoint.compareTo(realtimePoint) > 0);

            if (reachAlertWhenUp || reachAlertWhenDown) {
                BigDecimal chgPtg = targetPtg.subtract(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

                // alert now
                String msg = "股票到达设定的条件啦：设定在" + DateUtils.formatDate(p.getCreateTime()) + "的" + code
                    + "的点位是" + pointWhenAlert
                    + "，当前点位是" + realtimePoint
                    + "，目标涨跌幅度是" + chgPtg + "%"
                    + "，实际涨跌幅度为" + realChgPtg + "%";
                saveAlertLog(p, realtimePoint, realChgPtg);
                alertMsgToApp(msg);
            }
        }
    }

    private void saveAlertLog(StockAlertPersist p, BigDecimal realtimePoint, BigDecimal chgPtg) {
        StockAlertLogPersist alertLogPersist = new StockAlertLogPersist();
        alertLogPersist.initUserIdAndTime(p.getCreateUserId());
        alertLogPersist.setAlertId(p.getId());
        alertLogPersist.setConfigPoint(p.getCurrentPoint());
        alertLogPersist.setTargetPoint(realtimePoint);
        alertLogPersist.setTargetPtg(chgPtg);
        stockAlertLogDAO.persist(alertLogPersist);
    }

    private void alertMsgToApp(String msg) {
        String url = "http://go.valuetodays.cn/api/public/notify/anon/alertStock.do";
        log.info("ALERT::: alert msg: {}", msg);
        Map<String, String> payload = Map.of("msg", msg);
        String s = HttpClient4Utils.doPostJson(url, payload, "utf8");
        log.info("alert response: {}", s);
    }

}
