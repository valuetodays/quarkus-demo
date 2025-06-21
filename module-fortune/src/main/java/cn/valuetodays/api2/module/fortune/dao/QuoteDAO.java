package cn.valuetodays.api2.module.fortune.dao;


import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2023-04-02 13:12
 */
@ApplicationScoped
public class QuoteDAO implements PanacheRepository<QuotePO> {

    public QuotePO findByCode(String code) {
        return find("code = ?1", code).firstResult();
    }
}
