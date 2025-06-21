package cn.valuetodays.api2.web.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.service.EtfInfoServiceImpl;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-07-10 19:40
 */
@QuarkusTest
class EtfInfoServiceImplTest {

    @Inject
    private EtfInfoServiceImpl etfInfoService;

    @Test
    void refresh() {
        etfInfoService.refresh();
    }

    @Test
    void step2FillLastTradeAmount() {
        etfInfoService.step2FillLastTradeAmount();
    }

}
