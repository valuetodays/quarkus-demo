package cn.valuetodays.api2.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.service.StockTradeServiceImpl;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
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
}
