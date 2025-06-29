package cn.valuetodays.api2.basic.controller;

import java.util.List;

import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-11-01
 */
@Slf4j
@Path(value = "/basic/sql")
public class SqlController {
    @Inject
    private SqlServiceImpl sqlService;

    @Path(value = "/saveBySqls.do")
    @POST
    public AffectedRowsResp saveBySqls(@RequestBody List<String> sqls) {
        return sqlService.saveBySqls(sqls);
    }
}
