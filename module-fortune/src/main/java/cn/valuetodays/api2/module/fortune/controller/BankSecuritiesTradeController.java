package cn.valuetodays.api2.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.BankSecuritiesTradePersist;
import cn.valuetodays.api2.module.fortune.reqresp.StatEarnedMonthlyResp;
import cn.valuetodays.api2.module.fortune.reqresp.StatEarnedResp;
import cn.valuetodays.api2.module.fortune.service.BankSecuritiesTradeServiceImpl;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * 银证交易服务
 *
 * @author lei.liu
 * @since 2024-08-19 17:32
 */
@Path("/bankSecuritiesTrade")
public class BankSecuritiesTradeController extends BaseCrudController<
    Long, BankSecuritiesTradePersist, BankSecuritiesTradeServiceImpl> {

    @Path(value = "/parseTextAndSave")
    @POST
    public AffectedRowsResp parseTextAndSave(SimpleTypesReq req) {
        return getService().parseTextAndSave(req, getCurrentAccountId());
    }

    @Path(value = "/statEarned")
    @POST
    public List<StatEarnedResp> statEarned() {
        Long currentAccountId = getCurrentAccountId();
        return getService().statEarned(currentAccountId);
    }

    @Path(value = "/statEarnedMonthly")
    @POST
    public List<StatEarnedMonthlyResp> statEarnedMonthly() {
        Long currentAccountId = getCurrentAccountId();
        return getService().statEarnedMonthly(currentAccountId);
    }
}
