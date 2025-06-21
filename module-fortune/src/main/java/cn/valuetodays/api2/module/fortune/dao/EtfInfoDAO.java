package cn.valuetodays.api2.module.fortune.dao;


import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.EtfInfoPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2021-10-22 16:21
 */
@ApplicationScoped
public class EtfInfoDAO implements PanacheRepository<EtfInfoPO> {
    public EtfInfoPO findByCode(String code) {
        return find("code = ?1", code).firstResult();
    }

    public List<EtfInfoPO> pageQuery(Page page, Sort sort) {
        return findAll(sort).page(page).list();
    }
}
