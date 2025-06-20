package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.StockPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * .
 *
 * @author lei.liu
 * @since 2021-01-27 14:50
 */
@ApplicationScoped
public class StockDAO implements PanacheRepository<StockPO> {
    public StockPO findByCode(String code) {
        return find("code = ?1", code).firstResult();
    }

    public List<StockPO> findAllByCodeIn(List<String> codes) {
        return find("code in ?1", codes).list();
    }
}
