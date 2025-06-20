package cn.valuetodays.api2.module.fortune.service;

import java.util.List;

import cn.valuetodays.api2.module.fortune.dao.StockSubjectDAO;
import cn.valuetodays.api2.module.fortune.enums.StockSubjectEnums;
import cn.valuetodays.api2.module.fortune.persist.StockSubjectPO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * 股票标的
 *
 * @author lei.liu
 * @since 2024-05-02 11:33
 */
@ApplicationScoped
@Slf4j
public class StockSubjectServiceImpl
    extends BaseCrudService<Long, StockSubjectPO, StockSubjectDAO> {

    public List<StockSubjectPO> findAllByType(StockSubjectEnums.Type type) {
        return getRepository().findAllByType(type);
    }
}
