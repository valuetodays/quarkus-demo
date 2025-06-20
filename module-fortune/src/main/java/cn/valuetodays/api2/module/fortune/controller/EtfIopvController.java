package cn.valuetodays.api2.module.fortune.controller;

import cn.valuetodays.api2.module.fortune.service.EtfIopvService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-12-21
 */
@Path("/etfIopv")
public class EtfIopvController {

    @Inject
    EtfIopvService etfIopvService;

    @Path(value = "/refreshFully")
    @POST
//    @Async
    public void refreshFully() {
        etfIopvService.gatherAndSave(true);
    }
}
