package cn.valuetodays.api2.module.fortune.dao;


import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-21
 */
@ApplicationScoped
public class StockTradeDAO implements PanacheRepository<StockTradePO> {

    public List<StockTradePO> findAllByCode(String code) {
        return find("code = ?1", code).list();
    }

    public List<StockTradePO> findAllByCodeAndHedgeId(String code, Long hedgeId) {
        return find("code = ?1 and hedgeId = ?2", code, hedgeId).list();
    }

    //    @Query("SELECT DISTINCT e.code FROM StockTradePO e")
    public List<String> findDistinctCodes() {
        return find("SELECT DISTINCT e.code FROM StockTradePO e").project(String.class).list();
    }

    public List<StockTradePO> findAllByHedgeId(long hedgeId) {
        return find("hedgeId = ?1", hedgeId).list();
    }

    public List<StockTradePO> findAllByHedgeIdAndTradeDateGreaterThan(long hedgeId, int tradeDate) {
        return find("hedgeId = ?1 and tradeDate > ?2", hedgeId, tradeDate).list();
    }

    public List<StockTradePO> findAllByHedgeIdGreaterThanOrderByIdDesc(long hedgeId) {
        return find("hedgeId > ?1", Sort.descending("id"), hedgeId).list();
    }

    public List<StockTradePO> findAllByCodeAndHedgeIdGreaterThanOrderByIdDesc(String code, long hedgeId) {
        return find("code = ?1 and hedgeId > ?2", Sort.descending("id"), code, hedgeId).list();
    }

    //    @Query("SELECT COUNT(DISTINCT e.tradeDate) FROM StockTradePO e where e.hedgeId > 0")
    public int countTradedDays() {
        return find("SELECT COUNT(DISTINCT e.tradeDate) FROM StockTradePO e where e.hedgeId > 0").project(Integer.class).firstResult();
    }

    public List<StockTradePO> findByAccountIdAndIdIn(Long accountId, List<Long> ids) {
        return find("accountId = ?1 and id in ?2", accountId, ids).list();
    }

}
