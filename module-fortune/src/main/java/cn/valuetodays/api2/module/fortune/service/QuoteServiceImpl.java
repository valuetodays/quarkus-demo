package cn.valuetodays.api2.module.fortune.service;

import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.QuoteDAO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteLastConstituentsReq;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteLastConstituentsResp;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-04-06 12:11
 */
@ApplicationScoped
public class QuoteServiceImpl
    extends BaseCrudService<Long, QuotePO, QuoteDAO> {

    @Inject
    QuoteConstituentServiceImpl quoteConstituentService;

    public QuoteLastConstituentsResp getQuoteConstituentCodes(QuoteLastConstituentsReq req) {
        final QuoteLastConstituentsResp resp = new QuoteLastConstituentsResp();
        String quoteCode = req.getQuoteCode();
        if (StringUtils.isBlank(quoteCode)) {
            return resp;
        }
        QuotePO quote = getRepository().findByCode(quoteCode);
        if (Objects.isNull(quote)) {
            return resp;
        }
        resp.setQuoteConstituents(quoteConstituentService.findLastListByQuoteId(quote.getId()));
        return resp;
    }


}
