package cn.valuetodays.api2.module.fortune.component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.component.reqresp.StockForGuXiReq;
import cn.valuetodays.api2.module.fortune.component.reqresp.StockForGuXiResp;
import cn.valuetodays.api2.module.fortune.controller.StockRealtimeController;
import cn.valuetodays.api2.module.fortune.reqresp.EtfsRealtimeEtfsQuoteReq;
import cn.valuetodays.api2.module.fortune.reqresp.EtfsRealtimeEtfsQuoteResp;
import cn.vt.rest.third.utils.NumberUtils;
import jakarta.inject.Inject;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-16
 */
public abstract class BaseRongZiModule {
    // 融资利息
    public static final BigDecimal rzRate = BigDecimal.valueOf(5.0 / 100.0);
    public static final BigDecimal rzMoney = BigDecimal.valueOf(50000);
    private StockRealtimeController stockRealtimeController;

    @Inject
    public void setStockRealtimeFeignController(StockRealtimeController stockRealtimeController) {
        this.stockRealtimeController = stockRealtimeController;
    }

    public abstract List<StockForGuXiReq> determineStockList();

    public final List<StockForGuXiResp> computeDetail() {
        List<StockForGuXiReq> stockList = determineStockList();
        List<String> codes = stockList.stream().map(StockForGuXiReq::getCode).toList();

        final Map<String, StockForGuXiReq> mapAndInfoMap = stockList.stream()
            .collect(Collectors.toMap(StockForGuXiReq::getCode, e -> e));

        EtfsRealtimeEtfsQuoteReq req = new EtfsRealtimeEtfsQuoteReq();
        req.setCodes(codes);
        List<EtfsRealtimeEtfsQuoteResp> respList = stockRealtimeController.realtimeEtfsQuote(req);
        return respList.stream()
            .map(e -> computeDetail(mapAndInfoMap, e))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(StockForGuXiResp::getGuziRate).reversed())
            .toList();
    }

    public final StockForGuXiResp computeDetail(Map<String, StockForGuXiReq> mapAndInfoMap,
                                                EtfsRealtimeEtfsQuoteResp resp) {
        StockForGuXiReq stockForGuXiReq = mapAndInfoMap.get(resp.getCode());
        if (stockForGuXiReq == null) {
            return null;
        }
        StockForGuXiResp d = new StockForGuXiResp();
        d.setCode(resp.getCode());
        d.setName(stockForGuXiReq.getName());
        d.setRzMoney(rzMoney.intValue());
        d.setPrice(resp.getCurrent());
        d.setQuantity(computeQuantity(d.getRzMoney(), d.getPrice()));
        d.setRealUseMoney(d.getQuantity() * d.getPrice());
        d.setLast10Pai(stockForGuXiReq.getLast10Pai());
        // lastPai是每10派，所以要除以10，乘以100.0是计算百分比
        d.setGuziRate(NumberUtils.divide4(d.getLast10Pai() * 100.0 / 10.0, d.getPrice()).doubleValue());
        d.setNextFenhongMoney(
            BigDecimal.valueOf(d.getLast10Pai())
                .multiply(BigDecimal.valueOf(d.getQuantity()))
                .divide(BigDecimal.TEN, 3, RoundingMode.DOWN)
                .doubleValue()
        );
        d.setRzInterest(BigDecimal.valueOf(d.getRealUseMoney()).multiply(rzRate).doubleValue());
        d.setBigDeal(d.getNextFenhongMoney().compareTo(d.getRzInterest()) > 0);
        return d;
    }

    /**
     * 计算股数，最终股数是100的整数倍
     */
    protected final int computeQuantity(int money, Double price) {
        return (int) (money / price / 100) * 100;
    }
}
