package cn.valuetodays.api2.module.fortune.service;

import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.enums.StockTradeMonitorEnums;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradeMonitorPO;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AutoToHedgeTradeResp;
import cn.valuetodays.api2.module.fortune.dao.StockTradeMonitorDAO;
import cn.valuetodays.api2.module.fortune.service.kits.StockTradeAutoHedger;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-10
 */
@Slf4j
@ApplicationScoped
public class StockTradeMonitorServiceImpl
    extends BaseCrudService<Long, StockTradeMonitorPO, StockTradeMonitorDAO> {
    @Inject
    StockTradeServiceImpl stockTradeService;

    public List<StockTradeMonitorPO> findNormal() {
        return getRepository().findAllByStatus(StockTradeMonitorEnums.Status.NORMAL);
    }


    @Transactional
    public Boolean markAsTraded(SimpleTypesReq markAsTradedReq) {
        Long id = markAsTradedReq.getL();
        if (Objects.isNull(id) || id < 0) {
            return false;
        }
        StockTradeMonitorPO old = getRepository().findById(id);
        if (Objects.isNull(old)) {
            return false;
        }
        StockTradeMonitorEnums.Status status = old.getStatus();
        if (StockTradeMonitorEnums.Status.CLOSED == status || Objects.nonNull(old.getTradeSysid())) {
            return false;
        }
        old.setStatus(StockTradeMonitorEnums.Status.CLOSED);
        String sysid = markAsTradedReq.getText();
        if (Objects.nonNull(sysid)) {
            old.setTradeSysid(sysid);
        }
        getRepository().persist(old);
        return StockTradeMonitorEnums.Status.CLOSED == old.getStatus();
    }

    public AutoToHedgeTradeResp tryHedge() {
        AutoToHedgeTradeResp resp = new AutoToHedgeTradeResp();

        List<StockTradeMonitorPO> monitorList = this.findNormal();
        if (CollectionUtils.isEmpty(monitorList)) {
            resp.setRecords(0);
            return resp;
        }
        List<Long> tradeIdList = monitorList.stream()
            .map(StockTradeMonitorPO::getTradeId)
            .distinct()
            .toList();
        if (CollectionUtils.isEmpty(tradeIdList)) {
            resp.setRecords(0);
            return resp;
        }
        List<StockTradePO> list = stockTradeService.listByIds(tradeIdList);
        StockTradeAutoHedger autoHedger = new StockTradeAutoHedger();
        autoHedger.doAutoHedge(list, resp);
        return resp;
    }

}
