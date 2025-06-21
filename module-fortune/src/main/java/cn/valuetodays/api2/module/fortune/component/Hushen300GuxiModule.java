package cn.valuetodays.api2.module.fortune.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.component.reqresp.StockForGuXiReq;
import cn.valuetodays.api2.module.fortune.persist.QuoteConstituentPO;
import cn.valuetodays.api2.module.fortune.persist.StockDividendPO;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteLastConstituentsReq;
import cn.valuetodays.api2.module.fortune.reqresp.QuoteLastConstituentsResp;
import cn.valuetodays.api2.module.fortune.service.QuoteServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockDividendServiceImpl;
import cn.vt.rest.third.StockEnums;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.ObjectUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-15
 */
@ApplicationScoped
public class Hushen300GuxiModule extends BaseRongZiModule {
    @Inject
    QuoteServiceImpl quoteService;
    @Inject
    StockDividendServiceImpl stockDividendService;

    @Override
    public List<StockForGuXiReq> determineStockList() {
        final var req = new QuoteLastConstituentsReq();
        // 沪深300指数的代码
        req.setQuoteCode("000300");
        final QuoteLastConstituentsResp resp = quoteService.getQuoteConstituentCodes(req);

        List<QuoteConstituentPO> quoteConstituents = resp.getQuoteConstituents();
        final Map<String, QuoteConstituentPO> mapForQC = quoteConstituents.stream()
            .collect(Collectors.toMap(QuoteConstituentPO::getCode, e -> e));
        final List<StockDividendPO> stockDividends =
            stockDividendService.listLastDividends(new ArrayList<>(mapForQC.keySet()));
        final Map<String, BigDecimal> mapForSD = stockDividends.stream()
            .collect(Collectors.toMap(StockDividendPO::getCode, StockDividendPO::getPaiPerTen));
        return mapForQC.values().stream().map(e -> {
            StockForGuXiReq stockForGuXiReq = new StockForGuXiReq();
            StockEnums.Region region = StockEnums.Region.byCode(e.getCode());
            StockEnums.Region regionToUse =
                ObjectUtils.defaultIfNull(region, StockEnums.Region.SHANGHAI);
            stockForGuXiReq.setCode(regionToUse.getShortCode() + e.getCode());
            stockForGuXiReq.setName(e.getName());
            stockForGuXiReq.setLast10Pai(mapForSD.getOrDefault(e.getCode(), BigDecimal.ZERO).doubleValue());
            return stockForGuXiReq;
        }).toList();
    }
}
