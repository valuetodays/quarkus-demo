package cn.vt.rest.third.xueqiu;

import java.util.List;
import java.util.Objects;

import cn.vt.rest.third.StockEnums;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteData;
import cn.vt.rest.third.xueqiu.vo.XueQiuStockRealtimeQuoteResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 雪球股票实时报价-基础功能.
 *
 * @author lei.liu
 * @since 2023-05-23 10:50
 */
@Slf4j
public class StockRealtimeQuoteComponent {
    /**
     * 获取实时数据
     *
     * @param codes 可以以SH/SZ开头，如SH600036，也可是是600036
     */
    public static List<XueQiuStockRealtimeQuoteData> doGet(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return List.of();
        }
        List<String> codeWithRegionList = codes.stream()
            .map(e -> {
                // 上海/深圳都是以s开头
                if (StringUtils.startsWithIgnoreCase(e, StockEnums.Region.SHANGHAI.name().substring(1))) {
                    return e;
                }
                StockEnums.Region region = StockEnums.Region.byCode(e);
                if (Objects.isNull(region)) {
                    return e;
                }
                return region.getRegionShortCode() + e;
            })
            .distinct()
            .toList();
        String codesStr = String.join(",", codeWithRegionList);
//        log.debug("get realtimeQuote for {}", codesStr);
        XueQiuStockRealtimeQuoteResp respObj = XueQiuStockClientUtils.realtimeQuote(codesStr);
        int errorCode = respObj.getError_code();
        if (errorCode != 0) {
            log.warn("access xueqiu error: {}", respObj.getError_description());
            return List.of();
        }
        return respObj.getData();
    }

}
