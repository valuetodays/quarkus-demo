package cn.valuetodays.api2.module.fortune.component;

import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.reqresp.SaveUpStockReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.SaveUpStockResp;
import cn.vt.test.TestBase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SaveUpStockModule}.
 *
 * @author lei.liu
 * @since 2024-08-17
 */
public class SaveUpStockModuleTest extends TestBase {
    @Test
    void doSaveUp() {
        SaveUpStockReq req = new SaveUpStockReq();
        req.setBuyPrice(BigDecimal.valueOf(0.405));
        req.setBuyQuantity(50000);
        req.setCategory(SaveUpStockReq.Category.ETF);
        SaveUpStockModule module = new SaveUpStockModule();
        List<SaveUpStockResp.PossibleItem> list = module.doSaveUp(req);
        for (SaveUpStockResp.PossibleItem obj : list) {
            getLog().info("obj:{}", obj);
        }
    }
}
