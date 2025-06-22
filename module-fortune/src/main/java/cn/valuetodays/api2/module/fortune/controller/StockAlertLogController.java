package cn.valuetodays.api2.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.client.persist.StockAlertLogPersist;
import cn.valuetodays.api2.module.fortune.service.StockAlertLogService;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Path;

/**
 * 股票告警记录表服务
 *
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@RequestScoped
@Path("/stockAlertLog")
public class StockAlertLogController
    extends BaseCrudController<
    Long,
    StockAlertLogPersist,
    StockAlertLogService
    > {

}
