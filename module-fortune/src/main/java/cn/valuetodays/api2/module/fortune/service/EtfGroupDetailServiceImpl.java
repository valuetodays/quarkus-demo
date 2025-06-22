package cn.valuetodays.api2.module.fortune.service;

import cn.valuetodays.api2.module.fortune.client.persist.EtfGroupDetailPO;
import cn.valuetodays.api2.module.fortune.dao.EtfGroupDetailDAO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * etf组-详情
 *
 * @author lei.liu
 * @since 2023-05-31 18:03
 */
@ApplicationScoped
public class EtfGroupDetailServiceImpl
    extends BaseCrudService<Long, EtfGroupDetailPO, EtfGroupDetailDAO> {

}
