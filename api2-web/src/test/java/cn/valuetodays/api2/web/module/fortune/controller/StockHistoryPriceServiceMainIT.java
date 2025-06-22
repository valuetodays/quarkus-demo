package cn.valuetodays.api2.web.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.client.persist.StockPO;
import cn.valuetodays.api2.module.fortune.service.StockHistoryPriceServiceImpl;
import cn.vt.rest.third.StockEnums;
import cn.vt.rest.third.quote.CookieClientUtils;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2022-09-22
 */
public class StockHistoryPriceServiceMainIT {
    // 调用会失败，因为接口需要登录
    @Test
    public void refreshOneByXueQiu() {
        StockPO stock = new StockPO();
        stock.setCode("159605");
        stock.setRegion(StockEnums.Region.SHENZHEN);
        String loginCookie = CookieClientUtils.pullXueqiuCookies();
        new StockHistoryPriceServiceImpl().refreshOneByXueQiu(stock, loginCookie, 0);
    }

    @Test
    public void refreshOneByCs() {
        StockPO stock = new StockPO();
        stock.setCode("000001");
        stock.setRegion(StockEnums.Region.SHANGHAI);
        new StockHistoryPriceServiceImpl().refreshOneByCs(stock);
    }

}
