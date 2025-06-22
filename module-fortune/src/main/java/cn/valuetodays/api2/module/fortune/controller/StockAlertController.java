package cn.valuetodays.api2.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.client.persist.StockAlertPersist;
import cn.valuetodays.api2.module.fortune.service.StockAlertService;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Path;

/**
 * 股票告警服务
 *
 * @author lei.liu
 * @since 2025-04-16 08:40
 */
@RequestScoped
@Path("/stockAlert")
public class StockAlertController
    extends BaseCrudController<
    Long,
    StockAlertPersist,
    StockAlertService
    > {

}
