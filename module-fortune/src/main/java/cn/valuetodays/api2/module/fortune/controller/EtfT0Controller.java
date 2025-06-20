package cn.valuetodays.api2.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.persist.EtfT0PO;
import cn.valuetodays.api2.module.fortune.service.EtfT0Service;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-04
 */

@Path("etfT0")
public class EtfT0Controller
    extends BaseCrudController<Long, EtfT0PO, EtfT0Service> {

    @Path(value = "/refresh")
    @POST
    public Boolean refresh() {
        getService().refresh();
        return true;
    }

}
