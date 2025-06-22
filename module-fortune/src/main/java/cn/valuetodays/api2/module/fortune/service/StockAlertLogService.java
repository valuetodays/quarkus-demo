package cn.valuetodays.api2.module.fortune.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.persist.StockAlertLogPersist;
import cn.valuetodays.api2.module.fortune.client.persist.StockAlertPersist;
import cn.valuetodays.api2.module.fortune.dao.StockAlertLogDAO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.valuetodays.quarkus.commons.base.jpa.JpaIdBasePersist;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 股票告警记录表
 *
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@ApplicationScoped
@Slf4j
public class StockAlertLogService
    extends BaseCrudService<Long, StockAlertLogPersist, StockAlertLogDAO> {

    @Inject
    private StockAlertService stockAlertService;

    @Override
    protected void afterQuery(List<StockAlertLogPersist> list) {
        List<Long> alertIds = list.stream().map(StockAlertLogPersist::getAlertId).toList();
        if (CollectionUtils.isEmpty(alertIds)) {
            return;
        }
        List<StockAlertPersist> stockAlertPersists = stockAlertService.listByIds(alertIds);
        if (CollectionUtils.isEmpty(stockAlertPersists)) {
            return;
        }
        Map<Long, StockAlertPersist> idPersistMap = stockAlertPersists.stream()
            .collect(Collectors.toMap(JpaIdBasePersist::getId, e -> e));
        for (StockAlertLogPersist alertLog : list) {
            Long alertId = alertLog.getAlertId();
            StockAlertPersist stockAlertPersist = idPersistMap.get(alertId);
            if (Objects.nonNull(stockAlertPersist)) {
                alertLog.getOtherMap().put("alertLog", stockAlertPersist);
            }
        }
    }

}
