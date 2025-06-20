package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.EtfT0PO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * @author lei.liu
 * @since 2021-10-22 16:21
 */
@ApplicationScoped
public class EtfT0DAO implements PanacheRepository<EtfT0PO> {
    public EtfT0PO findByCode(String code) {
        return find("code = ?1", code).firstResult();
    }

    public List<EtfT0PO> findAllByCodeStartsWith(String prefix) {
        return find("code like ?1", prefix + "%").list();
    }

    public List<EtfT0PO> findAllByCodeLike(String codeVar) {
        return find("code like ?1", "%" + codeVar + "%").list();
    }
}
