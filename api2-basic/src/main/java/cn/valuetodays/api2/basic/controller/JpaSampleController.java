package cn.valuetodays.api2.basic.controller;

import cn.valuetodays.api2.basic.persist.JpaSamplePersist;
import cn.valuetodays.api2.basic.service.JpaSampleService;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * jpa查询示例服务
 *
 * @author lei.liu
 * @since 2025-06-02 07:43
 */
@Tag(name = "jpa示例")
@Path("/basic/jpaSample")
public class JpaSampleController
    extends BaseCrudController<Long, JpaSamplePersist, JpaSampleService> {

}
