package cn.valuetodays.api2.module.fortune.dao;

import java.math.BigDecimal;
import java.util.Optional;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradeInterestPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-17
 */
@ApplicationScoped
public class StockTradeInterestDAO implements PanacheRepository<StockTradeInterestPO> {
    public BigDecimal sumAllInterest() {
        BigDecimal o = find("select sum(o.amountFee) as amountFee from StockTradeInterestPO o")
            .project(BigDecimal.class)
            .firstResult();
        return Optional.ofNullable(o).orElse(BigDecimal.ZERO);
    }
}
