package cn.valuetodays.api2.module.fortune.service.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.valuetodays.api2.module.fortune.reqresp.EtfsRealtimeEtfsQuoteResp;
import cn.valuetodays.api2.module.fortune.reqresp.StockReaRltimeCompareReq;
import cn.valuetodays.api2.module.fortune.reqresp.StockRealtimeCompareResp;
import cn.vt.rest.third.xueqiu.StockRealtimeQuoteComponent;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-24
 */
@Slf4j
public class StockRealtimeCompareComponent {

    public static StockRealtimeCompareResp doCompare(StockReaRltimeCompareReq req) {
        List<String> codes = req.getItems().stream()
            .flatMap(e -> Stream.of(e.getCode1(), e.getCode2()))
            .distinct()
            .toList();
        List<XueQiuStockRealtimeQuoteData> stockRealtimeQuoteList = StockRealtimeQuoteComponent.doGet(codes);
        Map<String, EtfsRealtimeEtfsQuoteResp> codeAndPriceMap = stockRealtimeQuoteList.stream()
            .collect(Collectors.toMap(XueQiuStockRealtimeQuoteData::getSymbol, e -> {
                EtfsRealtimeEtfsQuoteResp resp = new EtfsRealtimeEtfsQuoteResp();
                resp.setCode(e.getSymbol());
                resp.setCurrent(e.getCurrent());
                resp.setPercent(e.getPercent());
                return resp;
            }));

        List<StockRealtimeCompareResp.ItemResp> respItemList = new ArrayList<>();
        List<StockReaRltimeCompareReq.Item> compareList = req.getItems();
        for (StockReaRltimeCompareReq.Item it : compareList) {
            String code1 = it.getCode1();
            String code2 = it.getCode2();
            double offsetPercent = it.getOffsetPercent();
            EtfsRealtimeEtfsQuoteResp realtimeInfo1 = codeAndPriceMap.get(code1);
            EtfsRealtimeEtfsQuoteResp realtimeInfo2 = codeAndPriceMap.get(code2);
            StockRealtimeCompareResp.ItemResp itemResp = StockRealtimeCompareResp.ofItem(code1, code2, "-");
            if (!ObjectUtils.allNotNull(realtimeInfo1, realtimeInfo2)) {
                continue;
            }
            Double percentInRealtimeInfo1 = realtimeInfo1.getPercent();
            Double percentInRealtimeInfo2 = realtimeInfo2.getPercent();
            if (!ObjectUtils.allNotNull(percentInRealtimeInfo1, percentInRealtimeInfo2)) {
                continue;
            }
            double realOffset = Math.abs(percentInRealtimeInfo1 - percentInRealtimeInfo2);
            if (realOffset >= offsetPercent) {
                String suggest = "sell " + code2 + " & buy " + code1;
                if (percentInRealtimeInfo1 > percentInRealtimeInfo2) {
                    suggest = "sell " + code1 + " & buy " + code2;
                }
                itemResp.setSuggest(suggest);
            }
            itemResp.setOffsetPercentReal(realOffset);
            itemResp.setOffsetPercentSet(offsetPercent);
            itemResp.setInfo1(realtimeInfo1);
            itemResp.setInfo2(realtimeInfo2);
            respItemList.add(itemResp);
        }

        StockRealtimeCompareResp resp = new StockRealtimeCompareResp();
        resp.setItems(respItemList);
        return resp;
    }

}

