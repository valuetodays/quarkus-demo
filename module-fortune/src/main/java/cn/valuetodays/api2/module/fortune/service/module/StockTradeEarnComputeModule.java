package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AnalyzeHedgedTradeResp;
import cn.valuetodays.api2.module.fortune.service.StockTradeServiceImpl;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-11-04
 */
public class StockTradeEarnComputeModule {

    private static <E> List<E> concat(final E e, final List<E> list) {
        List<E> r = new ArrayList<>(list.size() + 1);
        r.addAll(list);
        r.add(e);
        return r;
    }

    private RemoveDuplicateVo removeDuplicate(ArrayListValuedHashMap<Long, Long> hedgeIdsAndIdMap,
                                              Map<Long, StockTradePO> idMap) {
        // hedgeIdsAndIdMap中有如下这样的重复数据
        // 2 -> 3,4
        // 3 -> 2
        // 或者
        // 11 -> 12
        // 12 -> 11
        // 当使用了每一条后，不应该再重复使用第二条了
        //
        // 冰哥策略：当有4条及以上数据，且各有买卖时，需要特殊处理

        RemoveDuplicateVo removeDuplicateVo = new RemoveDuplicateVo();
        Set<Long> hedgeIds = hedgeIdsAndIdMap.keySet();
        final List<Long> distinctHedgeIds = new ArrayList<>();
        final List<Long> abandonHedgeIds = new ArrayList<>();
        for (Long hedgeId : hedgeIds) {
            if (abandonHedgeIds.contains(hedgeId)) {
                continue;
            }
            // 假如是复杂配对，就只取其中一条处理即可
            Set<Long> complexPairsSet = tryFindComplexPairs_NvsM(hedgeId, hedgeIdsAndIdMap, idMap);
            if (complexPairsSet.size() >= 4) {
                Long first = complexPairsSet.iterator().next();
                distinctHedgeIds.add(first);
                abandonHedgeIds.addAll(CollectionUtils.removeAll(complexPairsSet, Set.of(first)));
                removeDuplicateVo.getComplexHedgeIds().add(complexPairsSet);
                continue;
            }
            List<Long> idsOfHedgeId = hedgeIdsAndIdMap.get(hedgeId);
            List<Long> hedgeIdsOfIdsOfHedgeId = idsOfHedgeId.stream()
                .flatMap(e -> hedgeIdsAndIdMap.get(e).stream())
                .toList();
            if (hedgeIdsOfIdsOfHedgeId.contains(hedgeId) && hedgeIdsOfIdsOfHedgeId.size() > 1) {
                distinctHedgeIds.addAll(idsOfHedgeId);
                abandonHedgeIds.add(hedgeId);
            } else {
                distinctHedgeIds.add(hedgeId);
                abandonHedgeIds.addAll(idsOfHedgeId);
            }
        }

        removeDuplicateVo.setDistinctHedgeIds(distinctHedgeIds.stream().distinct().toList());
        return removeDuplicateVo;
    }

    private Set<Long> tryFindComplexPairs_NvsM(Long hedgeId,
                                               ArrayListValuedHashMap<Long, Long> hedgeIdsAndIdMap,
                                               Map<Long, StockTradePO> idMap) {
        Set<Long> poSet = new HashSet<>();
        tryFindComplexPairs_NvsM_0(poSet, hedgeId, hedgeIdsAndIdMap);
        // 以交易类型分组
        Map<Integer, List<StockTradePO>> map = poSet.stream()
            .map(idMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(p -> p.getTradeType().buyValue()));
        boolean greaterThanOrEquals2Items = map.values().stream().allMatch(e -> e.size() >= 2);
        // 必须有买卖两种类型且，每类都得有2条及以上
        if (greaterThanOrEquals2Items && map.keySet().size() >= 2) {
            return poSet;
        }
        return Set.of();
    }

    private void tryFindComplexPairs_NvsM_0(Set<Long> poSet, Long hedgeId,
                                            ArrayListValuedHashMap<Long, Long> hedgeIdsAndIdMap) {
        if (poSet.contains(hedgeId)) {
            return;
        }
        poSet.add(hedgeId);
        Map<Long, Collection<Long>> map = hedgeIdsAndIdMap.asMap();
        for (Map.Entry<Long, Collection<Long>> entry : map.entrySet()) {
            Long key = entry.getKey();
            Collection<Long> value = entry.getValue();
            if (value.contains(hedgeId)) {
                tryFindComplexPairs_NvsM_0(poSet, key, hedgeIdsAndIdMap);
            }
        }
        List<Long> longs = hedgeIdsAndIdMap.get(hedgeId);
        poSet.addAll(longs);
        for (Long aLong : longs) {
            tryFindComplexPairs_NvsM_0(poSet, aLong, hedgeIdsAndIdMap);
        }
    }


    private BigDecimal computeNetEarnedWithoutCommission(StockTradePO e) {
        // 股票变成钱 是正数
        BigDecimal fee = e.getGuohuFee().add(e.getYinhuaFee()).add(e.getYongjinFee()).negate();
        return computeEarnedWithCommission(List.of(e), fee);
    }

    /**
     * @param fee 手续费，应该是负数
     */
    private BigDecimal computeEarnedWithCommission(List<StockTradePO> list, BigDecimal fee) {
        List<BigDecimal> amounts = list.stream().map(e -> {
            FortuneCommonEnums.TradeType tradeType = e.getTradeType();
            // 钱变成股票 是负数
            // 股票变成钱 是正数
            BigDecimal amount = e.getPrice().multiply(BigDecimal.valueOf(e.getQuantity()));

            if (tradeType.isBuyFlag()) {
                amount = amount.negate();
            }
            return amount.add(fee);
        }).toList();
        return amounts.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal computeNetEarned(List<StockTradePO> tradesForHedgeId, List<StockTradePO> tradesForIds) {
        List<StockTradePO> list = ListUtils.union(tradesForHedgeId, tradesForIds);
        return list.stream()
            .map(this::computeNetEarnedWithoutCommission)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal computeCommission(List<StockTradePO> tradesForHedgeId, List<StockTradePO> tradesForIds) {
        List<StockTradePO> list = ListUtils.union(tradesForHedgeId, tradesForIds);
        return list.stream()
            .map(e -> e.getYongjinFee().add(e.getGuohuFee()).add(e.getYinhuaFee()))
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private Integer computeEarnedStockQuantity(List<StockTradePO> trades1,
                                               List<StockTradePO> trades2) {
        if (trades1.size() > 1) {
            return null;
        }
        StockTradePO trade1 = trades1.get(0);
        String codeForHedgedId = trade1.getCode();
        List<String> codesForIds = trades2.stream()
            .map(StockTradePO::getCode)
            .distinct()
            .toList();
        // 同一品种时才计算攒股数量
        if (codesForIds.size() == 1 && codesForIds.contains(codeForHedgedId)) {
            int quantityForTrade1 = trade1.getQuantity();
            FortuneCommonEnums.TradeType tradeType = trade1.getTradeType();
            /*
            计算盈利数量时，应该特殊处理分红，如下数据
            ------
            BUY 1000 1
            PASSIVE_SELL 1000 0.1
            SELL 1000 0.95
            ------
            一次BUY对应PASSIVE_SELL和SELL。
            所以：当计算盈利数量时，需要将PASSIVE_SELL的去掉，不然数据会不准
             */
            int quantityForTrades2 = trades2.stream()
                .filter(e -> e.getTradeType() != FortuneCommonEnums.TradeType.PASSIVE_SELL)
                .mapToInt(StockTradePO::getQuantity)
                .sum();
            if (tradeType.isBuyFlag()) {
                return quantityForTrade1 - quantityForTrades2;
            } else {
                return quantityForTrades2 - quantityForTrade1;
            }
        }
        return null;
    }

    private BigDecimal computeEarnedWithCommission(List<StockTradePO> tradesForHedgeId,
                                                   List<StockTradePO> tradesForIds) {
        List<StockTradePO> list = ListUtils.union(tradesForHedgeId, tradesForIds);
        return computeEarnedWithCommission(list, BigDecimal.ZERO);
    }

    public AnalyzeHedgedTradeResp doComputeEarn(List<StockTradePO> hedgedList) {
        return this.doComputeEarn(hedgedList, null);
    }

    public AnalyzeHedgedTradeResp doComputeEarn(List<StockTradePO> hedgedList, StockTradeServiceImpl stockTradeService) {
        final Map<Long, StockTradePO> idMap = hedgedList.stream()
            .collect(Collectors.toMap(StockTradePO::getId, e -> e));
        final ArrayListValuedHashMap<Long, Long> hedgeIdsAndIdMap = new ArrayListValuedHashMap<>();
        hedgedList.forEach(e -> hedgeIdsAndIdMap.put(e.getHedgeId(), e.getId()));
        RemoveDuplicateVo removeDuplicateVo = removeDuplicate(hedgeIdsAndIdMap, idMap);
        final List<Long> distinctHedgeIds = removeDuplicateVo.getDistinctHedgeIds();
        List<Set<Long>> complexHedgeIds = removeDuplicateVo.getComplexHedgeIds();

        List<AnalyzeHedgedTradeResp.Item> analyzeItemList = new ArrayList<>();

        BigDecimal sumWithCommission = BigDecimal.ZERO; // 含手续费的总额
        BigDecimal totalCommission = BigDecimal.ZERO; // 手续费总额
        BigDecimal totalNetEarnedCash = BigDecimal.ZERO; // 赚的现金总额（去除手续费）
        for (Long hedgeId : distinctHedgeIds) {
            List<Long> ids = null;
            List<Long> hedgeIds = null;
            Set<Long> currentComplexHedgeIds = null;
            // 优先处理冰哥策略
            for (Set<Long> complexHedgeId : complexHedgeIds) {
                if (complexHedgeId.contains(hedgeId)) {
                    currentComplexHedgeIds = complexHedgeId;
                }
            }
            if (CollectionUtils.isNotEmpty(currentComplexHedgeIds)) {
                List<StockTradePO> list = currentComplexHedgeIds.stream()
                    .map(idMap::get)
                    .filter(Objects::nonNull)
                    .toList();
                Map<Integer, List<StockTradePO>> mapByBuyValue = list.stream()
                    .collect(Collectors.groupingBy(e -> e.getTradeType().buyValue()));
                List<List<Long>> idListList = mapByBuyValue.values()
                    .stream().map(e -> e.stream().map(StockTradePO::getId).toList())
                    .toList();
                if (mapByBuyValue.keySet().size() != 2) {
                    throw new IllegalArgumentException("mapByBuyValue的key的数量应该是2");
                }
                ids = idListList.get(0);
                hedgeIds = idListList.get(1);
            } else {
                ids = hedgeIdsAndIdMap.get(hedgeId);
                hedgeIds = List.of(hedgeId);
            }
            Function<Long, StockTradePO> functionForGetById = ei -> {
                StockTradePO cached = idMap.get(ei);
                if (Objects.isNull(cached)) {
                    StockTradePO db = stockTradeService.findById(ei);
                    if (Objects.nonNull(db)) {
                        idMap.putIfAbsent(ei, db);
                        return db;
                    }
                }
                return cached;
            };
            AnalyzeHedgedTradeResp.Item analyzeItem = new AnalyzeHedgedTradeResp.Item();
            List<StockTradePO> tradesForHedgeId = hedgeIds.stream().map(functionForGetById).filter(Objects::nonNull).toList();
            List<StockTradePO> tradesForIds = ids.stream().map(functionForGetById).filter(Objects::nonNull).toList();
            if (CollectionUtils.isEmpty(tradesForHedgeId) || CollectionUtils.isEmpty(tradesForIds)) {
                analyzeItem.setErrorMsg("tradesForHedgeId 或 tradesForIds 为empty");
                analyzeItemList.add(analyzeItem);
                continue;
            }
            List<Integer> tradeTypeBuyValueList = tradesForIds.stream()
                .map(StockTradePO::getTradeType)
                .map(FortuneCommonEnums.TradeType::buyValue)
                .distinct()
                .toList();
            String idsAsString = StringUtils.join(ids, ",");
            analyzeItem.setHedgeIds(StringUtils.join(hedgeIds, ","));
            analyzeItem.setVsIds(idsAsString);
            if (tradeTypeBuyValueList.size() != 1) {
                analyzeItem.setErrorMsg("id为" + idsAsString + "的数据有误，tradeType应该一致");
                analyzeItemList.add(analyzeItem);
                continue;
            }
            int buyValues = tradesForHedgeId.get(0).getTradeType().buyValue() + tradeTypeBuyValueList.get(0);
            if (buyValues != 1) {
                analyzeItem.setErrorMsg("id为【" + hedgeId + "】,【" + idsAsString + "】的数据有误，"
                    + "tradeType应该相反");
                analyzeItemList.add(analyzeItem);
                continue;
            }

            BigDecimal earnedWithCommission = computeEarnedWithCommission(tradesForHedgeId, tradesForIds);
            BigDecimal netEarnedCash = computeNetEarned(tradesForHedgeId, tradesForIds);
            BigDecimal bdForCommission = computeCommission(tradesForHedgeId, tradesForIds);
            Integer earnedStockQuantity = computeEarnedStockQuantity(tradesForHedgeId, tradesForIds);
            analyzeItem.setEarnedStockQuantity(earnedStockQuantity);
            analyzeItem.setNetEarnedCash(netEarnedCash);
            analyzeItem.setCommission(bdForCommission);
            analyzeItem.setEarned(earnedWithCommission);
            if (netEarnedCash.compareTo(BigDecimal.ZERO) > 0) {
                if (tradesForHedgeId.size() == 1 && tradesForIds.size() == 1) {
                    StockTradePO tmp1 = tradesForHedgeId.get(0);
                    StockTradePO tmp2 = tradesForIds.get(0);
                    if (tmp1.getCode().equals(tmp2.getCode())
                        && tmp1.getQuantity() == tmp2.getQuantity()
                    ) {
                        BigDecimal tmp1Price = tmp1.getPrice();
                        BigDecimal tmp2Price = tmp2.getPrice();
                        BigDecimal tmpOffset = tmp1Price.subtract(tmp2Price).abs();
                        if (tmpOffset.compareTo(BigDecimal.valueOf(0.001)) > 0) {
                            analyzeItem.setErrorMsg("成功的交易，但是买卖价的差额是" + tmpOffset);
                        }
                    }
                }
            } else {
                analyzeItem.computeErrorMsg();
            }

            analyzeItemList.add(analyzeItem);
            totalNetEarnedCash = totalNetEarnedCash.add(netEarnedCash);
            totalCommission = totalCommission.add(bdForCommission);
            sumWithCommission = sumWithCommission.add(earnedWithCommission);
        }
        AnalyzeHedgedTradeResp resp = new AnalyzeHedgedTradeResp();
        resp.setTotalEarned(sumWithCommission);
        resp.setTotalNetEarnedCash(totalNetEarnedCash);
        resp.setItemList(analyzeItemList);
        resp.setTotalCommission(totalCommission);
        return resp;
    }

    @Data
    private static class RemoveDuplicateVo {
        private List<Long> distinctHedgeIds;
        private List<Set<Long>> complexHedgeIds = new ArrayList<>();
    }
}
