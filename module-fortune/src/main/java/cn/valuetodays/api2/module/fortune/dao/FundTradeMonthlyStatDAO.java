package cn.valuetodays.api2.module.fortune.dao;


import cn.valuetodays.api2.module.fortune.client.persist.FundTradeMonthlyStatPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-30
 */
@ApplicationScoped
public class FundTradeMonthlyStatDAO implements PanacheRepository<FundTradeMonthlyStatPO> {

}
