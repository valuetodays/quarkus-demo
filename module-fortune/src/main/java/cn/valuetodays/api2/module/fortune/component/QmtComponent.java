package cn.valuetodays.api2.module.fortune.component;

import java.time.LocalDate;

import cn.valuetodays.api2.module.fortune.client.reqresp.OrderInfo;
import cn.valuetodays.api2.module.fortune.service.StockTradeMonitorServiceImpl;
import cn.vt.util.DateUtils;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-02-18
 */
@ApplicationScoped
public class QmtComponent {
    @Inject
    StockTradeMonitorServiceImpl stockTradeMonitorService;

    public boolean markAsTraded(OrderInfo orderInfo) {
        // 若和tradeMonitor中的数据匹配，则记录
        String orderRemark = orderInfo.getOrder_remark();
        if (StringUtils.startsWith(orderRemark, "mid")) {
            String tradeMonitorId = orderRemark.substring("mid".length());
            SimpleTypesReq req = new SimpleTypesReq();
            Integer currentDate = DateUtils.formatAsYyyyMMdd(LocalDate.now());
            req.setText(currentDate + orderInfo.getOrder_sysid());
            req.setL(Long.valueOf(tradeMonitorId));
            return stockTradeMonitorService.markAsTraded(req);
        }
        return false;
    }
}
