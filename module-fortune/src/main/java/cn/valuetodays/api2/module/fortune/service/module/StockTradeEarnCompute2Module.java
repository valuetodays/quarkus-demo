package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
public class StockTradeEarnCompute2Module {

    private RemoveDuplicateVo removeDuplicate(ArrayListValuedHashMap<Long, Long> hedgeIdsAndIdMap,
                                              Map<Long, StockTradeServiceImpl.TmpVo> idMap) {
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
                                               Map<Long, StockTradeServiceImpl.TmpVo> idMap) {
        Set<Long> poSet = new HashSet<>();
        tryFindComplexPairs_NvsM_0(poSet, hedgeId, hedgeIdsAndIdMap);
        // 以交易类型分组
        Map<Integer, List<StockTradeServiceImpl.TmpVo>> map = poSet.stream()
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

    private BigDecimal computeNetEarnedWithoutCommission(StockTradeServiceImpl.TmpVo e) {
        // 出去的钱，变成负的
        BigDecimal fee = e.getTotalCommission().negate();
        return computeEarnedWithCommission(List.of(e), fee);
    }

    /**
     * @param fee 手续费，应该是负数
     */
    private BigDecimal computeEarnedWithCommission(List<StockTradeServiceImpl.TmpVo> list, BigDecimal fee) {
        List<BigDecimal> amounts = list.stream().map(e -> e.getAmountWithSign().add(fee)).toList();
        return amounts.stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private BigDecimal computeNetEarned(List<StockTradeServiceImpl.TmpVo> tradesForHedgeId,
                                        List<StockTradeServiceImpl.TmpVo> tradesForIds) {
        List<StockTradeServiceImpl.TmpVo> list = ListUtils.union(tradesForHedgeId, tradesForIds);
        return list.stream()
            .map(this::computeNetEarnedWithoutCommission)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal computeCommission(List<StockTradeServiceImpl.TmpVo> tradesForHedgeId,
                                         List<StockTradeServiceImpl.TmpVo> tradesForIds) {
        List<StockTradeServiceImpl.TmpVo> list = ListUtils.union(tradesForHedgeId, tradesForIds);
        return list.stream()
            .map(StockTradeServiceImpl.TmpVo::getTotalCommission)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    @Deprecated
    private Integer computeEarnedStockQuantity(List<StockTradeServiceImpl.TmpVo> trades1,
                                               List<StockTradeServiceImpl.TmpVo> trades2) {

        return null;
    }

    private BigDecimal computeEarnedWithCommission(List<StockTradeServiceImpl.TmpVo> tradesForHedgeId,
                                                   List<StockTradeServiceImpl.TmpVo> tradesForIds) {
        List<StockTradeServiceImpl.TmpVo> list = ListUtils.union(tradesForHedgeId, tradesForIds);
        return computeEarnedWithCommission(list, BigDecimal.ZERO);
    }

    public AnalyzeHedgedTradeResp doComputeEarn(List<StockTradeServiceImpl.TmpVo> hedgedList) {
        final Map<Long, StockTradeServiceImpl.TmpVo> idMap = hedgedList.stream()
            .collect(Collectors.toMap(StockTradeServiceImpl.TmpVo::getId, e -> e));
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
                List<StockTradeServiceImpl.TmpVo> list = currentComplexHedgeIds.stream()
                    .map(idMap::get)
                    .filter(Objects::nonNull)
                    .toList();
                Map<Integer, List<StockTradeServiceImpl.TmpVo>> mapByBuyValue = list.stream()
                    .collect(Collectors.groupingBy(e -> e.getTradeType().buyValue()));
                List<List<Long>> idListList = mapByBuyValue.values().stream()
                    .map(e -> e.stream().map(StockTradeServiceImpl.TmpVo::getId).toList())
                    .toList();
                if (mapByBuyValue.keySet().size() != 2) {
                    throw new IllegalArgumentException("mapByBuyValue的key的数量应该是2");
                }

                ids = idListList.get(FortuneCommonEnums.TradeType.SELL.buyValue());
                hedgeIds = idListList.get(FortuneCommonEnums.TradeType.BUY.buyValue());
            } else {
                ids = hedgeIdsAndIdMap.get(hedgeId);
                hedgeIds = List.of(hedgeId);
            }
            AnalyzeHedgedTradeResp.Item analyzeItem = new AnalyzeHedgedTradeResp.Item();
            List<StockTradeServiceImpl.TmpVo> tradesForHedgeId = hedgeIds.stream().map(idMap::get).filter(Objects::nonNull).toList();
            List<StockTradeServiceImpl.TmpVo> tradesForIds = ids.stream().map(idMap::get).filter(Objects::nonNull).toList();
            if (CollectionUtils.isEmpty(tradesForHedgeId) || CollectionUtils.isEmpty(tradesForIds)) {
                analyzeItem.setErrorMsg("tradesForHedgeId 或 tradesForIds 为empty");
                analyzeItemList.add(analyzeItem);
                continue;
            }
            List<Integer> tradeTypeBuyValueList = tradesForIds.stream()
                .map(StockTradeServiceImpl.TmpVo::getTradeType)
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
            if (netEarnedCash.compareTo(BigDecimal.ZERO) <= 0) {
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
