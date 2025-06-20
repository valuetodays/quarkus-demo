package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.StockMinutePricePO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-24 23:17
 */
@ApplicationScoped
public class StockMinutePriceDAO implements PanacheRepository<StockMinutePricePO> {
    //    @Query(value = " select new StockMinutePricePO(m.ts, m.avgPx, m.closePx) "
//        + " from StockMinutePricePO m where m.code=?1 order by m.ts desc")
    public List<StockMinutePricePO> findAllByCodeOrderByTsDesc(String code) {
        return list("code = ?1", Sort.descending("ts"), code);
    }
}
