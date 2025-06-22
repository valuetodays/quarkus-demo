package cn.valuetodays.api2.module.fortune.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeHedgeEarnedChartReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeHedgeEarnedChartResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeHedgedTradeResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeHedgedWithFeeResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeTradeTimeDistributeChartReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeTradeTimeDistributeChartResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.AutoToHedgeTradeResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.CheckHedgedKitsResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.GetStockTradeInfosReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.GetStockTradeInfosResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.SaveTradeMonitorByTradeResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.TryAutoToHedgeReq;
import cn.valuetodays.api2.module.fortune.service.StockTradeServiceImpl;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import cn.valuetodays.quarkus.commons.base.QuerySearch;
import cn.vt.vo.NameValueVo;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-19
 */
@Path("/stockTrade")
public class StockTradeController extends BaseCrudController<Long, StockTradePO, StockTradeServiceImpl> {

    @Path(value = "/parseTextAndSave")
    @POST
    public AffectedRowsResp parseTextAndSave(SimpleTypesReq req) {
        return service.parseTextAndSave(req, getCurrentAccountId());
    }


    @Path(value = "/findTrades.do")
    @POST
    public List<StockTradePO> findTrades(String code) {
        return getService().findTrades(code);
    }

    @Path(value = "/findNotHedge.do")
    @POST
    public List<StockTradePO> findNotHedge() {
        return getService().findNotHedge();
    }

    @Path(value = "/findCodes.do")
    @POST
    public List<NameValueVo> findCodes() {
        return getService().findCodes();
    }

    @Path(value = "/checkHedgedData.do")
    @POST
    public CheckHedgedKitsResp checkHedgedData(String code) {
        return getService().checkHedged(code);
    }

    /**
     * 自动对冲交易
     */
    @Path(value = "/autoToHedge.do")
    @POST
    public AutoToHedgeTradeResp autoToHedge(SimpleTypesReq req) {
        Integer days = req.getI();
        if (days <= 0) {
            days = 3;
        }
        AutoToHedgeTradeResp resp1 = getService().autoHedgePairTrade(null, days);
        AutoToHedgeTradeResp resp2 = getService().autoToHedge(days);
        return resp1.merge(resp2);
    }


    @Path(value = "/autoHedgeOrders.do")
    @POST
    public AutoToHedgeTradeResp autoHedgeOrders(List<StockTradePO> trades) {
        return StockTradeServiceImpl.autoHedgeOrders(trades);
    }

    @Path(value = "/tryAutoToHedge.do")
    @POST
    public AutoToHedgeTradeResp tryAutoToHedge(TryAutoToHedgeReq req) {
        return getService().tryAutoToHedge(req.getCode(), req.isDryRun());
    }

    //    @Async
    @Path(value = "/checkHedgedList.do")
    @POST
    public void checkHedgedList() {
        getService().checkHedgedList();
    }

    @Path(value = "/saveTradeMonitorByTrade.do")
    @POST
    public SaveTradeMonitorByTradeResp saveTradeMonitorByTrade(
        SimpleTypesReq req
    ) {
        return getService().saveTradeMonitorByTrade(req);
    }

    @Path(value = "/analyzeHedged.do")
    @POST
    public AnalyzeHedgedTradeResp analyzeHedged() {
        AnalyzeHedgedTradeResp resp = getService().analyzeHedged(getCurrentAccountId());
        resp.setTradedDays(getService().countTradedDays());
        resp.setTradedDaysWithWeekends(getService().countTradedDaysWithWeekends());
        return resp;
    }

    @Path(value = "/analyzeHedgedCode.do")
    @POST
    public AnalyzeHedgedTradeResp analyzeHedgedCode(List<QuerySearch> searchList) {
        AnalyzeHedgedTradeResp resp = getService().analyzeHedgedCode(searchList, getCurrentAccountId());
        resp.setTradedDays(getService().countTradedDays());
        resp.setTradedDaysWithWeekends(getService().countTradedDaysWithWeekends());
        return resp;
    }

    @Path("/analyzeHedgedWithFee.do")
    @POST
    public AnalyzeHedgedWithFeeResp analyzeHedgedWithFee() {
        AnalyzeHedgedTradeResp analyzeHedgedTrade = this.analyzeHedged();
        AnalyzeHedgedWithFeeResp resp = new AnalyzeHedgedWithFeeResp();
        resp.setAnalyzeHedgedTrade(analyzeHedgedTrade);

        // 从stockTradeInterestService.sumAllInterest();
        // interestValue 是负数
        BigDecimal interestValue = BigDecimal.valueOf(-100);
        BigDecimal netEarnedCash = analyzeHedgedTrade.getTotalNetEarnedCash().add(interestValue);
        resp.setNetEarnedCash(netEarnedCash);
        resp.setInterestValue(interestValue.abs());
        int tradedDays = analyzeHedgedTrade.getTradedDays();
        if (tradedDays > 0) {
            BigDecimal netEarnedCashPerTradeDay = netEarnedCash.divide(
                BigDecimal.valueOf(tradedDays), 2, RoundingMode.HALF_DOWN
            );
            resp.setNetEarnedCashPerTradeDay(netEarnedCashPerTradeDay);
        }
        int tradedDaysWithWeekends = analyzeHedgedTrade.getTradedDaysWithWeekends();
        if (tradedDaysWithWeekends > 0) {
            BigDecimal netEarnedCashPerTradeDayWithWeekends = netEarnedCash.divide(
                BigDecimal.valueOf(tradedDaysWithWeekends), 2, RoundingMode.HALF_DOWN
            );
            resp.setNetEarnedCashPerTradeDayWithWeekends(netEarnedCashPerTradeDayWithWeekends);
        }
        return resp;
    }

    @Path("/analyzeHedgeEarnedChart.do")
    @POST
    public AnalyzeHedgeEarnedChartResp analyzeHedgeEarnedChart(AnalyzeHedgeEarnedChartReq req) {
        return getService().analyzeHedgeEarnedChart(req);
    }

    @Path("/getStockTradeInfos.do")
    @POST
    public GetStockTradeInfosResp getStockTradeInfos(GetStockTradeInfosReq req) {
        List<StockTradePO> stockTrades = getService().findByAccountIdAndIdIn(req.getAccountId(), req.toIdList());
        stockTrades.forEach(e -> {
            e.setGuohuFee(null);
            e.setRemark(null);
            e.setYinhuaFee(null);
            e.setYongjinFee(null);
        });
        GetStockTradeInfosResp resp = new GetStockTradeInfosResp();
        resp.setTradeList(stockTrades);
        return resp;
    }

    @Path("/analyzeTradeTimeDistributeChart.do")
    @POST
    public AnalyzeTradeTimeDistributeChartResp analyzeTradeTimeDistributeChart(
        AnalyzeTradeTimeDistributeChartReq req
    ) {
        return getService().analyzeTradeTimeDistributeChart(req);
    }


}
