package cn.valuetodays.api2.module.fortune.dao;

import cn.valuetodays.api2.module.fortune.client.persist.EtfGroupDetailPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2023-05-31 18:03
 */
@ApplicationScoped
public class EtfGroupDetailDAO implements PanacheRepository<EtfGroupDetailPO> {

}
