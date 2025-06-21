package cn.valuetodays.api2.module.fortune.service.kits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.reqresp.CheckHedgedKitsResp;
import cn.valuetodays.quarkus.commons.base.jpa.JpaIdBasePersist;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 检查对冲数据是否匹配.
 *
 * @author lei.liu
 * @since 2024-02-03
 */
@Slf4j
public class CheckHedgedKits {
    public CheckHedgedKitsResp doCheck(List<StockTradePO> list) {
        List<CheckHedgedKitsResp.Result> resultList = new ArrayList<>();

        final List<Long> parsedIds = new ArrayList<>(list.size());
        for (StockTradePO obj : list) {
            if (!parsedIds.contains(obj.getId())) {
                String s = parseRelatedHedged(list, obj, parsedIds);
                if (StringUtils.isNotBlank(s)) {
                    resultList.add(CheckHedgedKitsResp.ofResult(null, s));
                }
            }
        }
        CheckHedgedKitsResp resp = new CheckHedgedKitsResp();
        resp.setResultList(resultList);
        return resp;
    }

    private String parseRelatedHedged(List<StockTradePO> list,
                                      StockTradePO obj,
                                      List<Long> parsedIds) {
        if (obj.markedAsHedged()) {
            return null;
        }
        if (obj.isGiveUpOppositeTrade()) {
            return null;
        }
        if (obj.wasNotHedged()) {
            String msg = "id=" + obj.getId() + ", not hedge";
            log.warn(msg);
            return msg;
        }

        final Map<Long, StockTradePO> map = list.stream()
            .collect(Collectors.toMap(JpaIdBasePersist::getId, e -> e));
        final Map<Long, List<StockTradePO>> mapByHedgeId = list.stream()
            .collect(Collectors.groupingBy(StockTradePO::getHedgeId));

        final Long hedgeId = obj.getHedgeId();
        StockTradePO inMap = map.get(hedgeId);
        if (Objects.isNull(inMap)) {
            String msg = "hedged data maybe wrong. ids:" + obj.getId() + "," + hedgeId;
            log.warn(msg);
            return msg;
        }
        if (!obj.getCode().equals(inMap.getCode())) {
            String msg = "maybe wrong, code is not same, ids:" + obj.getId() + "," + hedgeId;
            log.warn(msg);
            return msg;
        }


        final Set<Long> usedIds = new HashSet<>();
        aaa(obj, map, mapByHedgeId, usedIds);

        final Set<StockTradePO> relateds = usedIds.stream().map(map::get).collect(Collectors.toSet());
        boolean flag = StockTradeStatUtils.canBeTotallyHedged(relateds);
        if (!flag) {
            String msg = "maybe wrong, quantity not the same, ids:" + StringUtils.joinWith(", ", usedIds);
            log.warn(msg);
            return msg;
        } else {
            parsedIds.addAll(usedIds);
        }
        return null;
    }

    private void aaa(StockTradePO obj,
                     Map<Long, StockTradePO> map,
                     Map<Long, List<StockTradePO>> mapByHedgeId,
                     Set<Long> usedIds) {
        if (obj == null) {
            return;
        }
        Long id = obj.getId();
        if (usedIds.contains(id)) {
            return;
        }
        usedIds.add(id);
        aaa(map.get(obj.getHedgeId()), map, mapByHedgeId, usedIds);
        List<StockTradePO> stockTradePOS = mapByHedgeId.get(id);
        if (CollectionUtils.isNotEmpty(stockTradePOS)) {
            for (StockTradePO stockTradePO : stockTradePOS) {
                aaa(stockTradePO, map, mapByHedgeId, usedIds);
            }
        }
    }
}
