package cn.valuetodays.api2.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.component.ChiXiGuModule;
import cn.valuetodays.api2.module.fortune.component.Hushen300GuxiModule;
import cn.valuetodays.api2.module.fortune.component.reqresp.StockForGuXiResp;
import cn.valuetodays.api2.module.fortune.reqresp.ChiXiGuResp;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-30
 */
@Path(value = "/chiXiGu")
public class ChiXiGuController {
    @Inject
    ChiXiGuModule chiXiGuModule;
    @Inject
    Hushen300GuxiModule hushen300GuxiModule;

    @Path("/selfChoice.do")
    @POST
    public ChiXiGuResp selfChoice() {
        List<StockForGuXiResp> chiXiGuList = chiXiGuModule.computeDetail();
        ChiXiGuResp chiXiGuResp = new ChiXiGuResp();
        chiXiGuResp.setChiXiGuList(chiXiGuList);
        chiXiGuResp.setRzRate(ChiXiGuModule.rzRate);
        chiXiGuResp.setRzMoney(ChiXiGuModule.rzMoney);
        return chiXiGuResp;
    }

    @Path("/hushen300.do")
    @POST
    public ChiXiGuResp hushen300() {
        List<StockForGuXiResp> chiXiGuList = hushen300GuxiModule.computeDetail();
        ChiXiGuResp chiXiGuResp = new ChiXiGuResp();
        chiXiGuResp.setChiXiGuList(chiXiGuList);
        chiXiGuResp.setRzRate(ChiXiGuModule.rzRate);
        chiXiGuResp.setRzMoney(ChiXiGuModule.rzMoney);
        return chiXiGuResp;
    }

}
