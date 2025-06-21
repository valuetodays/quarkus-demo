package cn.valuetodays.api2.module.fortune.dao;

import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.valuetodays.api2.module.fortune.reqresp.AShareDailyTurnAmount;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


/**
 * @author lei.liu
 * @since 2023-04-02 13:02
 */
@ApplicationScoped
public class QuoteDailyStatDAO implements PanacheRepository<QuoteDailyStatPO> {

    @Inject
    SqlServiceImpl sqlService;

    public QuoteDailyStatPO findByCodeAndStatDate(String code, Integer statDate) {
        return find("code = ?1 and statDate = ?2", code, statDate).firstResult();
    }

    public List<QuoteDailyStatPO> findByCodeInAndStatDateGreaterThan(List<String> codes, int statDate) {
        return find("code in ?1 and statDate > ?2", codes, statDate).list();
    }

    public List<QuoteDailyStatPO> findByCodeInAndStatDate(List<String> codes, int statDate) {
        return find("code in ?1 and statDate = ?2", codes, statDate).list();
    }

    public List<AShareDailyTurnAmount> findAShareDailyTurnAmount() {
        String sql = "SELECT stat_date as minTime, sum(busi_money) as turnAmount "
            + " FROM " + QuoteDailyStatPO.TABLE_NAME
            + " WHERE `code` IN ('000001', '399001') "
            + " group by stat_date order by stat_date asc ";
        return sqlService.queryForList(sql, AShareDailyTurnAmount.class);
    }

    public List<QuoteDailyStatPO> findAllByCodeOrderByStatDateDesc(String code) {
        return find("code = ?1", Sort.descending("statDate"), code).list();
    }

}
