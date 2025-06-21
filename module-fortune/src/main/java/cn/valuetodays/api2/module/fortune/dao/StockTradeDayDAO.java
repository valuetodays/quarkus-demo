package cn.valuetodays.api2.module.fortune.dao;


import cn.valuetodays.api2.module.fortune.persist.StockTradeDayPO;
import cn.vt.rest.third.StockEnums;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2024-05-01 02:02
 */
@ApplicationScoped
public class StockTradeDayDAO implements PanacheRepository<StockTradeDayPO> {

    public StockTradeDayPO findByYearAndRegion(int year, StockEnums.Region region) {
        return find("year = ?1 and region = ?2", year, region).firstResult();
    }
}
