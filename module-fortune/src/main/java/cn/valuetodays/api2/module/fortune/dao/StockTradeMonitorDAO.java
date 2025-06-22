package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.enums.StockTradeMonitorEnums;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradeMonitorPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-10
 */
@ApplicationScoped
public class StockTradeMonitorDAO implements PanacheRepository<StockTradeMonitorPO> {

    public List<StockTradeMonitorPO> findAllByStatus(StockTradeMonitorEnums.Status status) {
        return list("status = ?1", status);
    }

    public List<StockTradeMonitorPO> findAllByStatusAndTradeSysidNotNull(StockTradeMonitorEnums.Status status) {
        return list("status = ?1 and tradeSysid is not null", status);
    }
}
