package cn.valuetodays.api2.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.client.persist.StockPO;
import cn.valuetodays.api2.module.fortune.service.StockServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-04
 */

@Path("/stock")
public class StockController
    extends BaseCrudController<Long, StockPO, StockServiceImpl> {


}
