package cn.valuetodays.api2.module.fortune.dao;


import cn.valuetodays.api2.module.fortune.client.persist.StockPairTradePO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2024-04-24 20:34
 */
@ApplicationScoped
public class StockPairTradeDAO implements PanacheRepository<StockPairTradePO> {

}
