package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockHoldersPO;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author lei.liu
 * @since 2023-01-10 21:38
 */
@ApplicationScoped
public class StockHoldersDAO implements PanacheRepository<StockHoldersPO> {

    public StockHoldersPO findByChannelAndStatDateAndSecCode(FortuneCommonEnums.Channel channel, int statDate, String secCode) {
        return find("channel = ?1 and statDate =?2 and secCode = ?3", channel, statDate, secCode).firstResult();
    }

    public List<StockHoldersPO> findAllBySecCodeOrderByStatDateAsc(String secCode) {
        return list("secCode = ?1", Sort.ascending("statDate"), secCode);
    }
}
