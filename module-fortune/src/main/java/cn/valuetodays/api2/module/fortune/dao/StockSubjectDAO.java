package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.enums.StockSubjectEnums;
import cn.valuetodays.api2.module.fortune.persist.StockSubjectPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * @author lei.liu
 * @since 2024-05-02 11:33
 */
@ApplicationScoped
public class StockSubjectDAO implements PanacheRepository<StockSubjectPO> {

    public List<StockSubjectPO> findAllByType(StockSubjectEnums.Type type) {
        return find("type = ?1", type).list();
    }
}
