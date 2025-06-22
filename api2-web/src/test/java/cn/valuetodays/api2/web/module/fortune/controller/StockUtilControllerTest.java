package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.reqresp.StockPriceGradientReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockPriceGradientResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockPriceGradientResp.GradientItem;
import cn.valuetodays.api2.module.fortune.controller.StockUtilController;
import cn.vt.R;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StockUtilController}.
 *
 * @author lei.liu
 * @since 2025-06-11
 */
@QuarkusTest
@Slf4j
public class StockUtilControllerTest {

    @Inject
    StockUtilController stockUtilController;

    @Test
    public void stockPriceGradient() {
        StockPriceGradientReq req = new StockPriceGradientReq();
        req.setCode("164701");
        req.setRangeValue(new BigDecimal(2));
        R<StockPriceGradientResp> r = stockUtilController.stockPriceGradient(req);
        StockPriceGradientResp data = r.getData();
//        log.info("data={}", data);
        String code = data.getCode();
        BigDecimal currentPrice = data.getCurrentPrice();
        List<GradientItem> gradients = data.getGradients();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---").append(code).append(" @ ").append(currentPrice);
        for (GradientItem gradient : gradients) {
            stringBuilder.append("\n  ").append(gradient.getChgPtg()).append("%  ").append(gradient.getPrice());
        }
        log.info(">> {}", stringBuilder);
    }
}
