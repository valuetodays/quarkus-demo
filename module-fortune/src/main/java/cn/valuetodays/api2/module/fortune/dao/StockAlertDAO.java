package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.enums.StockAlertEnums;
import cn.valuetodays.api2.module.fortune.client.persist.StockAlertPersist;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@ApplicationScoped
public class StockAlertDAO implements PanacheRepository<StockAlertPersist> {

    public List<StockAlertPersist> findAllByStatus(StockAlertEnums.Status status) {
        return list("status = ?1", status);
    }

    public List<StockAlertPersist> findAllByStatusAndScheduleType(StockAlertEnums.Status status,
                                                                  StockAlertEnums.ScheduleType scheduleType) {
        return list("status = ?1 and scheduleType = ?2", status, scheduleType);
    }
}
