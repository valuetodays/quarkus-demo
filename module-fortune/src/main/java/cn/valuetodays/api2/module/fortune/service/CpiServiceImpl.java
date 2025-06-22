package cn.valuetodays.api2.module.fortune.service;

import cn.valuetodays.api2.module.fortune.client.persist.CpiPO;
import cn.valuetodays.api2.module.fortune.dao.CpiDAO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * cpi数据
 *
 * @author lei.liu
 * @since 2023-01-13 16:58
 */
@ApplicationScoped
public class CpiServiceImpl
    extends BaseCrudService<Long, CpiPO, CpiDAO> {

}
