package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.EtfGroupPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * @author lei.liu
 * @since 2023-05-31 18:03
 */
@ApplicationScoped
public class EtfGroupDAO implements PanacheRepository<EtfGroupPO> {

    public List<EtfGroupPO> findAllByEnableFlag(boolean enableFlag) {
        return find("enableFlag=?1", enableFlag).list();
    }

    public List<EtfGroupPO> findAllByEnableFlagAndTn(boolean enableFlag, int tn) {
        return find("enableFlag=?1 and tn=?2", enableFlag, tn).list();

    }
}
