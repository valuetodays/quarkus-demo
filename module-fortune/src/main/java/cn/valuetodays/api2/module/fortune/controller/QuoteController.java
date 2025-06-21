package cn.valuetodays.api2.module.fortune.controller;

import java.util.Comparator;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.valuetodays.api2.module.fortune.reqresp.AShareHistoryTurnAmountResp;
import cn.valuetodays.api2.module.fortune.reqresp.AShareLatestTurnAmountChartResp;
import cn.valuetodays.api2.module.fortune.reqresp.AShareLatestTurnAmountResp;
import cn.valuetodays.api2.module.fortune.reqresp.CodedQuoteStatVo;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatReq;
import cn.valuetodays.api2.module.fortune.reqresp.DailyOffsetStatResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteLastConstituentsReq;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteLastConstituentsResp;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteStatGroupResp;
import cn.valuetodays.api2.module.fortune.service.QuoteDailyStatServiceImpl;
import cn.valuetodays.api2.module.fortune.service.QuoteServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-02 19:33
 */
@Slf4j
@Path(value = "/quote")
public class QuoteController {

    /**
     * 1亿
     */
    private static final Long YI = 100000000L;

    @Inject
    QuoteServiceImpl quoteService;
    @Inject
    QuoteDailyStatServiceImpl quoteDailyStatService;

    @Path(value = "/quoteList")
    @POST
    public List<QuotePO> quoteList() {
        return quoteService.list();
    }

    @Path(value = "/aShareLatestTurnAmount.do")
    @POST
    public AShareLatestTurnAmountResp aShareLatestTurnAmount() {
        return quoteDailyStatService.aShareLatestTurnAmount();
    }

    @Path(value = "/aShareLatestTurnAmountChart.do")
    @POST
    public AShareLatestTurnAmountChartResp aShareLatestTurnAmountChart() {
        AShareHistoryTurnAmountResp resp = quoteDailyStatService.aShareHistoryTurnAmount();
        List<AShareHistoryTurnAmountResp.DailyAmountData> list = resp.getList();
        List<AShareHistoryTurnAmountResp.DailyAmountData> sortedList = list.stream()
            .sorted(Comparator.comparing(AShareHistoryTurnAmountResp.DailyAmountData::getMinTime))
            .toList();
        AShareLatestTurnAmountChartResp chartResp = new AShareLatestTurnAmountChartResp();
        chartResp.setMinTimeList(
            sortedList.stream()
                .map(e -> {
                    String minTime = e.getMinTime();
                    return minTime.substring(0, 4) + "/" + minTime.substring(4, 6) + "/" + minTime.substring(6);
                })
                .toList()
        );
        chartResp.setAmountByYiList(
            sortedList.stream()
                .map(e -> e.getTurnAmount() / YI)
                .toList()
        );
        return chartResp;
    }

    @Path("/aShareHistoryTurnAmount.do")
    @POST
    public AShareHistoryTurnAmountResp aShareHistoryTurnAmount() {
        return quoteDailyStatService.aShareHistoryTurnAmount();
    }

    /**
     * 用法   /feign/refreshOne.do?code=xxx
     */
    @Path("/refreshOne.do")
    @POST
    public int refreshOne(@QueryParam("code") String code, @QueryParam("fully") @DefaultValue("true") Boolean fully) {
        return quoteDailyStatService.refreshOne(code, fully);
    }

    /**
     * 用法   /feign/refreshAll.do
     */
    @Path("/refreshAll.do")
    @POST
    public boolean refreshAll() {
        return quoteDailyStatService.refreshAll();
    }

    @Path("/listByCode.do")
    @POST
    public List<QuoteDailyStatPO> listByCode(String code) {
        return quoteDailyStatService.findAllByCodeOrderByMinTimeDesc(code);
    }

    @Path("/computeKeyPointsByCode.do")
    @POST
    public List<QuoteDailyStatPO> computeKeyPointsByCode(String code) {
        return quoteDailyStatService.computeKeyPointsByCode(code);
    }

    @Path("/quoteStats.do")
    @POST
    public QuoteStatGroupResp quoteStats(String code) {
        return quoteDailyStatService.computeStats(code);
    }

    @Path("/quoteStatsAllCodes.do")
    @POST
    public List<CodedQuoteStatVo> quoteStatsAllCodes() {
        return quoteDailyStatService.computeStatsForAllCodes();
    }

    @Path("/dailyOffsetStat.do")
    @POST
    public DailyOffsetStatResp dailyOffsetStat(DailyOffsetStatReq req) {
        return quoteDailyStatService.computeDailyOffsetStat(req);
    }

    @Path("/getQuoteLastConstituents.do")
    @POST
    public QuoteLastConstituentsResp getQuoteConstituentCodes(QuoteLastConstituentsReq req) {
        return quoteService.getQuoteConstituentCodes(req);
    }

}
