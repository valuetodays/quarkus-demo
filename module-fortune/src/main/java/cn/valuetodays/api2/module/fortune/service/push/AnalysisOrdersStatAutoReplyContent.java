package cn.valuetodays.api2.module.fortune.service.push;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.controller.QmtController;
import cn.valuetodays.api2.web.basic.push.vocechat.AutoReplyContent;
import cn.valuetodays.api2.web.basic.push.vocechat.PushBaseReq;
import cn.vt.fortune.modestep.client.vo.OrderVo;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-12-17
 */
@ApplicationScoped
public class AnalysisOrdersStatAutoReplyContent implements AutoReplyContent {
    @Inject
    private QmtController qmtController;

    @Override
    public List<String> title() {
        return List.of("统计委托的成交量成交额");
    }

    @Override
    public Pair<PushBaseReq.ContentType, String> replyContent(String value) {
        List<OrderVo> successOrders = qmtController.getSuccessOrders();
        List<StockTradePO> trades = successOrders.stream()
            .map(QmtController::fromOrderVo)
            .filter(Objects::nonNull)
            .toList();

        Map<String, List<StockTradePO>> map = trades.stream()
            .collect(Collectors.groupingBy(StockTradePO::getCode));
        String linedString = map.entrySet().stream()
            .map(entry -> {
                String code = entry.getKey();
                List<StockTradePO> tradesPartition = entry.getValue();
                Map<FortuneCommonEnums.TradeType, List<StockTradePO>> subMap =
                    tradesPartition.stream().collect(Collectors.groupingBy(StockTradePO::getTradeType));
                return code + "\n" + toLinedString(subMap);
            })
            .collect(Collectors.joining("\n"));
        return AutoReplyContent.makePlainText(linedString);
    }

    private String toLinedString(Map<FortuneCommonEnums.TradeType, List<StockTradePO>> subMap) {
        return subMap.entrySet().stream().map(e -> {
            FortuneCommonEnums.TradeType key = e.getKey();
            List<StockTradePO> value = e.getValue();
            return "\t" + key + " " + value.stream().mapToInt(StockTradePO::getQuantity).sum() + " shares"
                + " with " + value.stream().mapToDouble(ie -> ie.getQuantity() * ie.getPrice().doubleValue()).sum()
                + " yuan.";
        }).collect(Collectors.joining("\n"));
    }
}
