package cn.valuetodays.api2.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockPairTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockPairRealtimeCompareResp;
import cn.valuetodays.api2.module.fortune.service.StockPairTradeServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;


/**
 * 股票对买对卖交易服务
 *
 * @author lei.liu
 * @since 2024-04-24 20:34
 */
@Path("/stockPairTrade")
public class StockPairTradeController
    extends BaseCrudController<Long, StockPairTradePO, StockPairTradeServiceImpl> {

    @Path("/realtimeCompare")
    @POST
    public List<StockPairRealtimeCompareResp> realtimeCompare() {
        return getService().computeMonitors();
    }

}
