package cn.vt.rest.third.eastmoney;

import java.util.Objects;

import cn.vt.exception.AssertUtils;
import cn.vt.rest.third.StockEnums;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailData;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailDataTyped;
import cn.vt.rest.third.eastmoney.vo.EastMoneyStockDetailResp;
import cn.vt.util.HttpClient4Utils;
import cn.vt.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-27
 */
@Slf4j
public class EastMoneyStockModule {
    private EastMoneyStockModule() {
    }

    /**
     * realtime stock detail
     *
     * @param stockCode 600036
     * @see <a href="https://quote.eastmoney.com/sh513010.html">page</a>
     */
    public static EastMoneyStockDetailDataTyped getStockDetail(String stockCode) {
        StockEnums.Region region = StockEnums.Region.byCode(stockCode);
        AssertUtils.assertNotNull(region, "No right region for stock: " + stockCode);

        String url = "https://push2.eastmoney.com/api/qt/stock/get"
            + "?"
            + "invt=2"
            + "&fltt=1"
            + "&cb=jQuery35105191871482628052_1690429214388"
            + "&fields=f58%2Cf734%2Cf107%2Cf57%2Cf43%2Cf59%2Cf169%2Cf170%2Cf152%2Cf46%2Cf60%2Cf44%2Cf45%2Cf47%2Cf48%2Cf19%2Cf17%2Cf531%2Cf15%2Cf13%2Cf11%2Cf20%2Cf18%2Cf16%2Cf14%2Cf12%2Cf39%2Cf37%2Cf35%2Cf33%2Cf31%2Cf40%2Cf38%2Cf36%2Cf34%2Cf32%2Cf211%2Cf212%2Cf213%2Cf214%2Cf215%2Cf210%2Cf209%2Cf208%2Cf207%2Cf206%2Cf161%2Cf49%2Cf171%2Cf50%2Cf86%2Cf168%2Cf108%2Cf167%2Cf71%2Cf292%2Cf51%2Cf52%2Cf191%2Cf192%2Cf452%2Cf177"
            + "&secid=" + region.getSecid() + "." + stockCode
            + "&ut=fa5fd1943c7b386f172d6893dbfba10b"
            + "&wbp2u=%7C0%7C0%7C0%7Cweb"
            + "&_=" + System.currentTimeMillis();

        String responseString = HttpClient4Utils.doGet(url);
        String jsonString = EastMoneyCommons.substringBusiJsonString(responseString);
        EastMoneyStockDetailResp resp = JsonUtils.fromJson(jsonString, EastMoneyStockDetailResp.class);
        EastMoneyStockDetailData data = resp.getData();
        if (Objects.isNull(data)) {
            EastMoneyStockDetailData nullable = new EastMoneyStockDetailData();
            nullable.setCode(stockCode);
            nullable.setName(stockCode);
            nullable.setPrice("0");
            nullable.setAvgPrice("0");
            nullable.setOutPan("0");
            nullable.setInPan("0");
            nullable.setTradeAmount("0");
            nullable.setTradeVolume("0");
            data = nullable;
        }
        return data.toTyped();
    }

}
