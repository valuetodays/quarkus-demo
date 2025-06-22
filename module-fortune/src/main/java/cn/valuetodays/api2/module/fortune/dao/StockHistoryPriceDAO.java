package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockHistoryPricePO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * .
 *
 * @author lei.liu
 * @since 2021-03-19 18:50
 */
@ApplicationScoped
public class StockHistoryPriceDAO implements PanacheRepository<StockHistoryPricePO> {
    public StockHistoryPricePO findByCodeAndRegionAndMinTime(String code, String region, int minTime) {
        return find("code = ?1 and region = ?2 and minTime = ?3", code, region, minTime).firstResult();
    }

    public List<StockHistoryPricePO> findAllByCode(String code) {
        return find("code = ?1", code).list();
    }

    public StockHistoryPricePO findTop1ByCodeAndRegionOrderByMinTimeAsc(String code, String region) {
        return find("code = ?1 and region = ?2", Sort.descending("minTime"), code, region).firstResult();
    }
}
