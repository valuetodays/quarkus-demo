package cn.valuetodays.api2.web.module.fortune.controller;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.enums.FundTradeMonthlyStatEnums;
import cn.valuetodays.api2.module.fortune.client.persist.FundTradeMonthlyStatPO;
import cn.valuetodays.api2.module.fortune.dao.FundTradeMonthlyStatDAO;
import cn.valuetodays.api2.module.fortune.service.module.SSEFundTradeMonthlyModule;
import cn.vt.util.JsonUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SSEFundTradeMonthlyModule}.
 *
 * @author lei.liu
 * @since 2023-07-30
 */
@Slf4j
public class SSEFundTradeMonthlyModuleIT {

    @Inject
    SSEFundTradeMonthlyModule module;
    @Inject
    FundTradeMonthlyStatDAO fundTradeMonthlyStatDAO;

    /**
     * 不能直接访问对方api接口，
     */
    @Test
    void request() {
        module.request("2023-04");
    }

    /**
     * 需要自己访问浏览器，然后复制接口内容到此
     * http://www.sse.com.cn/market/funddata/overview/monthly/
     */
    @Test
    void parse() {
        String yearMonthStr = "2022-02";
        String data = ""; //getFileAsString("data/sse/funddata/overview-monthly/" + yearMonthStr + ".json");
        List<FundTradeMonthlyStatPO> poList = module.parse(yearMonthStr, data);
        for (FundTradeMonthlyStatPO po : poList) {
            log.info("-> {}", JsonUtils.toJson(po));
            if (po.getType() != FundTradeMonthlyStatEnums.Type.UNKNOWN) {
                fundTradeMonthlyStatDAO.persist(po);
            }
        }
    }

    /**
     * 需要自己访问浏览器，然后复制接口内容到此
     * http://www.sse.com.cn/market/funddata/overview/monthly/index_his.shtml
     */
    @Test
    void parseHistory() {
        String yearMonthStr = "2019-01";
        String data = ""; //getFileAsString("data/sse/funddata/overview-monthly/" + yearMonthStr + ".json");
        List<FundTradeMonthlyStatPO> poList = module.parseHistory(yearMonthStr, data);
        List<FundTradeMonthlyStatPO> distinctPoList = poList.stream().distinct().toList();
        for (FundTradeMonthlyStatPO po : distinctPoList) {
            log.info("-> {}", JsonUtils.toJson(po));
            if (po.getType() != FundTradeMonthlyStatEnums.Type.UNKNOWN) {
                try {
                    fundTradeMonthlyStatDAO.persist(po);
                } catch (Exception e) {

                }
            }
        }
    }


    @Test
    void parseHistoryByApi() {
        String yearMonthStr = "2022-01";
        List<FundTradeMonthlyStatPO> poList = module.requestHistory(yearMonthStr);
        for (FundTradeMonthlyStatPO po : poList) {
            log.info("-> {}", JsonUtils.toJson(po));
            if (po.getType() != FundTradeMonthlyStatEnums.Type.UNKNOWN) {
                fundTradeMonthlyStatDAO.persist(po);
            }
        }
    }
}
