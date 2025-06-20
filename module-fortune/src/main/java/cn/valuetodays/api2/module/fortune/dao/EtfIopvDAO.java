package cn.valuetodays.api2.module.fortune.dao;


import cn.valuetodays.api2.module.fortune.persist.EtfIopvPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2024-09-16 21:42
 */
@ApplicationScoped
public class EtfIopvDAO implements PanacheRepository<EtfIopvPO> {

}
