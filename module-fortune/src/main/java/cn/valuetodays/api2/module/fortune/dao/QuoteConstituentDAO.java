package cn.valuetodays.api2.module.fortune.dao;

import java.time.LocalDate;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.QuoteConstituentPO;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;


/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-16
 */
@ApplicationScoped
public class QuoteConstituentDAO implements PanacheRepository<QuoteConstituentPO> {

    public List<QuoteConstituentPO> findAllByQuoteIdAndStatDateAndCodeIn(Long quoteId, LocalDate statDate, List<String> codes) {
        return find("quoteId = ?1 and statDate = ?2 and code in ?3", quoteId, statDate, codes).list();
    }

    //    @Query("select distinct(code) from QuoteConstituentPO qc where qc.quoteId = ?1")
    public List<String> findCodesByQuoteId(Long quoteId) {
        return find("select distinct(code) from QuoteConstituentPO qc where qc.quoteId = ?1", quoteId).project(String.class).list();

    }

    public List<QuoteConstituentPO> findTop1ByQuoteIdOrderByStatDateDesc(Long quoteId) {
        return find("quoteId = ?1 ", Sort.descending("statDate"), quoteId).firstResult();
    }

    public List<QuoteConstituentPO> findAllByQuoteIdAndStatDate(Long quoteId, LocalDate statDate) {
        return find("quoteId = ?1 and statDate = ?2", quoteId, statDate).list();
    }
}
