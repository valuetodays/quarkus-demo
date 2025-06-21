package cn.valuetodays.api2.module.fortune.controller;


import cn.valuetodays.api2.module.fortune.persist.EtfInfoPO;
import cn.valuetodays.api2.module.fortune.service.EtfInfoServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-04
 */
@Path("/etfInfo")
public class EtfInfoController extends BaseCrudController<
    Long, EtfInfoPO,
    EtfInfoServiceImpl> {

    @Path(value = "/refresh")
    @POST
    public Boolean refresh() {
        getService().refresh();
        return true;
    }

}
