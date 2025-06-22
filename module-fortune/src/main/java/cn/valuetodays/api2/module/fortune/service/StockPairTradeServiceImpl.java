package cn.valuetodays.api2.module.fortune.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.valuetodays.api2.module.fortune.client.persist.StockPairTradePO;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsRealtimeEtfsQuoteReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsRealtimeEtfsQuoteResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockPairRealtimeCompareResp;
import cn.valuetodays.api2.module.fortune.controller.StockRealtimeController;
import cn.valuetodays.api2.module.fortune.dao.StockPairTradeDAO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;

/**
 * 股票对买对卖交易
 *
 * @author lei.liu
 * @since 2024-04-24 20:34
 */
@ApplicationScoped
@Slf4j
public class StockPairTradeServiceImpl
    extends BaseCrudService<Long, StockPairTradePO, StockPairTradeDAO> {

    @Inject
    StockTradeServiceImpl stockTradeService;
    @Inject
    // fixme: 不能注入上一层级的Controller，先这样写，后续会删除Feignclient,并提供对应的 StockRealtimeService
    StockRealtimeController stockRealtimeController;

    public List<StockPairRealtimeCompareResp> computeMonitors() {
        List<StockPairTradePO> stockPairTradeList = getRepository().listAll();
        List<StockPairTradePO> list = ObjectUtils.defaultIfNull(stockPairTradeList, List.of());
        List<PairObj> configs = list.stream()
            .map(e -> PairObj.of(e.getId(), Triple.of(e.getTradeId1(), e.getTradeId2(), e.getTradeId3())))
            .toList();

        List<Long> ids = configs.stream()
            .flatMap(PairObj::tradeIdsAsStream)
            .filter(e -> !Objects.equals(e, 0L))
            .distinct()
            .toList();
        List<StockTradePO> stockTradeList = stockTradeService.listByIds(ids);
        EtfsRealtimeEtfsQuoteReq req = new EtfsRealtimeEtfsQuoteReq();
        req.setCodes(stockTradeList.stream().map(StockTradePO::getCode).distinct().toList());
        List<EtfsRealtimeEtfsQuoteResp> etfsRealtimeList =
            stockRealtimeController.realtimeEtfsQuote(req);

        final Map<String, EtfsRealtimeEtfsQuoteResp> codeMap = etfsRealtimeList.stream()
            .collect(Collectors.toMap(e -> e.getCode().substring("SH".length()), e -> e));
        Map<Long, StockTradePO> idMap = stockTradeList.stream()
            .collect(Collectors.toMap(StockTradePO::getId, e -> e));
        return configs.stream()
            .map(e -> computeMonitor(e, codeMap, idMap))
            .filter(Objects::nonNull)
            .toList();
    }

    private StockPairRealtimeCompareResp computeMonitor(PairObj pairObj,
                                                        Map<String, EtfsRealtimeEtfsQuoteResp> codeMap,
                                                        Map<Long, StockTradePO> idMap) {
        Triple<Long, Long, Long> e = pairObj.getTradeIds();
        Long t1 = e.getLeft();
        Long t2 = e.getMiddle();
        Long t3 = e.getRight();
        StockTradePO firstTrade = idMap.get(t1);
        StockTradePO secondTrade = idMap.get(t2);
        int valSum = firstTrade.getTradeType().buyValue() + secondTrade.getTradeType().buyValue();
        // 如果前两笔交易不是一买一卖
        if (valSum != 1) {
            return null;
        }
        StockPairRealtimeCompareResp vo = new StockPairRealtimeCompareResp();
        vo.setStockPairTradeId(pairObj.getPairTradeId());
        if (firstTrade.getTradeType().buyValue() == 1) {
            vo.setBuyTrade(firstTrade);
            vo.setSellTrade(secondTrade);
        } else {
            vo.setBuyTrade(secondTrade);
            vo.setSellTrade(firstTrade);
        }
        StockTradePO buyTrade = vo.getBuyTrade();
        String buyCode = buyTrade.getCode();
        EtfsRealtimeEtfsQuoteResp etfsRealtimeForBuy = codeMap.get(buyCode);
        StockTradePO sellTrade = vo.getSellTrade();
        String sellCode = sellTrade.getCode();
        EtfsRealtimeEtfsQuoteResp etfsRealtimeForSell = codeMap.get(sellCode);
        // 设置默认价格
        vo.setCurrentBuyPrice(BigDecimal.valueOf(etfsRealtimeForBuy.getCurrent()));
        vo.setCurrentSellPrice(BigDecimal.valueOf(etfsRealtimeForSell.getCurrent()));
        // 如果有第三笔交易，就说明买价或卖价就已经固定了，就不能再使用实时价格
        StockTradePO thirdTrade = null;
        if (!Objects.equals(t3, 0L)) {
            thirdTrade = idMap.get(t3);
            // 这里第三笔交易是买的话，就对应实时卖价
            if (thirdTrade.getTradeType().buyValue() == 1) {
                vo.setCurrentSellPrice(thirdTrade.getPrice());
            } else {
                vo.setCurrentBuyPrice(thirdTrade.getPrice());
            }
        }

        BigDecimal part1EarnedBd = vo.getCurrentBuyPrice()
            .subtract(vo.getBuyTrade().getPrice())
            .multiply(BigDecimal.valueOf(vo.getBuyTrade().getQuantity()));
        BigDecimal part2EarnedBd = vo.getSellTrade().getPrice()
            .subtract(vo.getCurrentSellPrice())
            .multiply(BigDecimal.valueOf(vo.getSellTrade().getQuantity()));
        vo.setTotalEarned(part1EarnedBd.add(part2EarnedBd));
        vo.setPart1Earned(part1EarnedBd);
        vo.setPart2Earned(part2EarnedBd);
        BigDecimal feeInFormerTwoTrades = Stream.of(vo.getBuyTrade(), vo.getSellTrade())
            .map(this::computeFee)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
        BigDecimal totalFeeIn4Trades;
        if (Objects.isNull(thirdTrade)) {
            // 没有第三笔交易，说明就两笔交易，总共是4笔交易，所以手续费要乘以2
            totalFeeIn4Trades = feeInFormerTwoTrades.multiply(BigDecimal.valueOf(2));
        } else {
            // 有第三笔交易，总共是4笔交易，前两笔交易手续费 - 第三笔交易手续费 = 第四笔交易手续费（约等于）
            BigDecimal feeInTheThirdTrade = computeFee(thirdTrade);
            totalFeeIn4Trades = feeInFormerTwoTrades.add(feeInTheThirdTrade)
                .add(feeInFormerTwoTrades.subtract(feeInTheThirdTrade));
        }
        vo.setTotalFee(totalFeeIn4Trades);
        vo.setShouldTrade(vo.getTotalEarned().compareTo(vo.getTotalFee()) > 0);
        return vo;
    }

    private BigDecimal computeFee(StockTradePO trade) {
        return trade.getGuohuFee().add(trade.getYinhuaFee()).add(trade.getYongjinFee());
    }

    @Data
    private static class PairObj implements Serializable {
        private Long pairTradeId;
        private Triple<Long, Long, Long> tradeIds;

        public static PairObj of(Long pairTradeId, Triple<Long, Long, Long> tradeIds) {
            PairObj pairObj = new PairObj();
            pairObj.pairTradeId = pairTradeId;
            pairObj.tradeIds = tradeIds;
            return pairObj;
        }

        public Stream<Long> tradeIdsAsStream() {
            return Stream.of(tradeIds.getLeft(), tradeIds.getMiddle(), tradeIds.getRight());
        }
    }
}
