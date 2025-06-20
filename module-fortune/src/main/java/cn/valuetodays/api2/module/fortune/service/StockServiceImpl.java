package cn.valuetodays.api2.module.fortune.service;

import java.util.List;

import cn.valuetodays.api2.module.fortune.dao.StockDAO;
import cn.valuetodays.api2.module.fortune.persist.StockPO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-01-27 14:52
 */
@Slf4j
@ApplicationScoped
public class StockServiceImpl
    extends BaseCrudService<Long, StockPO, StockDAO> {

    public StockPO findByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return getRepository().findByCode(code);
    }

    public List<StockPO> findAllByCodes(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return List.of();
        }
        return getRepository().findAllByCodeIn(codes);
    }
}
