package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.CpiPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * @author lei.liu
 * @since 2023-01-13 16:58
 */
@ApplicationScoped
public class CpiDAO implements PanacheRepository<CpiPO> {
    public List<Integer> findAllYearMonth() {
        return find("select u.statYearMonth from CpiPO u").project(Integer.class).list();
    }
}
