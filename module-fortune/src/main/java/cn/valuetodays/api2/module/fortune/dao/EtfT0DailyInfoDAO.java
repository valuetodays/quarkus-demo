package cn.valuetodays.api2.module.fortune.dao;

import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.EtfT0DailyInfoPersist;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;


/**
 * @author lei.liu
 * @since 2025-01-04 11:44
 */
@ApplicationScoped
public class EtfT0DailyInfoDAO implements PanacheRepository<EtfT0DailyInfoPersist> {
    public EtfT0DailyInfoPersist findByCodeAndStatDate(String code, Integer statDate) {
        return find("code=?1 and statDate=?2", code, statDate).firstResult();
    }

    @Transactional
    public void updateTotalSharesById(BigDecimal totalVolumeInWan, Long id) {
        update("set totalSharesWan = ?1 where id = ?2", totalVolumeInWan, id);
    }

    public List<EtfT0DailyInfoPersist> findAllByStatDate(Integer statDate) {
        return list("statDate = ?1", statDate);
    }
}
