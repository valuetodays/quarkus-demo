package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteValuePO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * .
 *
 * @author lei.liu
 * @since 2021-08-17 16:26
 */
@ApplicationScoped
public class QuoteValueDAO implements PanacheRepository<QuoteValuePO> {

    public List<QuoteValuePO> findAllByCode(String code) {
        return find("code = ?1", code).list();
    }

    public QuoteValuePO findTop1ByCodeOrderByStatDateDesc(String code) {
        return find("code = ?1", Sort.descending("statDate"), code).firstResult();
    }
}
