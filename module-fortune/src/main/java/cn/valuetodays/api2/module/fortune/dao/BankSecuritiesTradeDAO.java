package cn.valuetodays.api2.module.fortune.dao;


import cn.valuetodays.api2.module.fortune.persist.BankSecuritiesTradePersist;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2024-08-19 17:32
 */
@ApplicationScoped
public class BankSecuritiesTradeDAO implements PanacheRepository<BankSecuritiesTradePersist> {

}
