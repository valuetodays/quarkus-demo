package cn.valuetodays.api2.module.fortune.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.QuoteConstituentDAO;
import cn.valuetodays.api2.module.fortune.dao.QuoteDAO;
import cn.valuetodays.api2.module.fortune.persist.QuoteConstituentPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.valuetodays.api2.module.fortune.service.module.EastMoneyQuoteConstituentModule;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-16
 */
@ApplicationScoped
@Slf4j
public class QuoteConstituentServiceImpl
    extends BaseCrudService<Long, QuoteConstituentPO, QuoteConstituentDAO> {

    @Inject
    StockDividendServiceImpl stockDividendService;
    @Inject
    QuoteDAO quoteDAO;

    //    @Async
    public void scheduleSaveDividends() {
        for (EastMoneyQuoteConstituentModule.Type type : EastMoneyQuoteConstituentModule.Type.values()) {
            saveDividends(type);
        }
    }

    public void saveDividends(EastMoneyQuoteConstituentModule.Type type) {
        QuotePO quotePO = quoteDAO.findByCode(type.getCode());
        if (Objects.isNull(quotePO)) {
            return;
        }
        List<String> codes = getRepository().findCodesByQuoteId(quotePO.getId());
        saveDividends(codes);
    }

    public void saveDividends(List<String> codes) {
        stockDividendService.parseAndSave(codes);
    }

    /**
     * 获取最新的成分股列表
     *
     * @param quoteId 指数ID
     */
    public List<QuoteConstituentPO> findLastListByQuoteId(Long quoteId) {
        final List<QuoteConstituentPO> empty = new ArrayList<>(0);
        List<QuoteConstituentPO> list = getRepository().findTop1ByQuoteIdOrderByStatDateDesc(quoteId);
        if (CollectionUtils.isEmpty(list)) {
            return empty;
        }
        QuoteConstituentPO quoteConstituentPO = list.get(0);
        if (Objects.isNull(quoteConstituentPO)) {
            return empty;
        }
        LocalDate statDate = quoteConstituentPO.getStatDate();
        if (Objects.isNull(statDate)) {
            return empty;
        }
        return getRepository().findAllByQuoteIdAndStatDate(quoteId, statDate);
    }

}
