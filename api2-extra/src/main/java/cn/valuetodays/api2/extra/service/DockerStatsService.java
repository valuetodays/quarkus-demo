package cn.valuetodays.api2.extra.service;

import cn.valuetodays.api2.extra.dao.DockerStatsRepository;
import cn.valuetodays.api2.extra.persist.DockerStatsPersist;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-24
 */
@ApplicationScoped
public class DockerStatsService extends BaseCrudService<Long, DockerStatsPersist, DockerStatsRepository> {

}
