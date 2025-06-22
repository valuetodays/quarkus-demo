package cn.valuetodays.api2.extra.dao;

import cn.valuetodays.api2.extra.persist.DockerStatsPersist;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-24
 */
@ApplicationScoped
public class DockerStatsRepository implements PanacheRepository<DockerStatsPersist> {
}
