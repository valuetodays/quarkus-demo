package cn.valuetodays.api2.module.fortune.dao;

import java.time.LocalDate;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.StockDividendPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * @author lei.liu
 * @since 2023-08-18
 */
@ApplicationScoped
public class StockDividendDAO implements PanacheRepository<StockDividendPO> {

    public List<StockDividendPO> findAllByCodeOrderByStatDateAsc(String code) {
        return find("code = ?1", Sort.ascending("statDate"), code).list();
    }

    public StockDividendPO findByCodeAndStatDate(String code, LocalDate statDate) {
        return find("code = ?1 and statDate = ?2", code, statDate).firstResult();
    }

    public List<StockDividendPO> findAllByCode(String code) {
        return find("code = ?1", code).list();
    }

    public List<StockDividendPO> findAllByCodeInAndStatDateGreaterThan(List<String> codes, LocalDate localDate) {
        return find("code in ?1 and statDate > ?2", codes, localDate).list();
    }
}
