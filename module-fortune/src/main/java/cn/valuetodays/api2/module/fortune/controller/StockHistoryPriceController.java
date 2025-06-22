package cn.valuetodays.api2.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockHistoryPricePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockMinutePriceFindAllByCodeReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockMinutePriceRefreshOneReq;
import cn.valuetodays.api2.module.fortune.service.StockHistoryPriceServiceImpl;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * @author lei.liu
 * @since 2022-09-23
 */
@Path("/stockHistoryPrice")
public class StockHistoryPriceController {
    @Inject
    StockHistoryPriceServiceImpl stockHistoryPriceService;

    @Path("/findAllByCode.do")
    @POST
    public List<StockHistoryPricePO> findAllByCode(@Valid StockMinutePriceFindAllByCodeReq req) {
        return stockHistoryPriceService.findAllByCode(req.getCode());
    }

    @Path("/refreshOneFully.do")
    @POST
//    @Async
    public void refreshOneFully(@Valid StockMinutePriceRefreshOneReq req) {
        stockHistoryPriceService.refreshOneFully(req.getCode(), null);
    }
}
