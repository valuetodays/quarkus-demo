package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.enums.FundTradeMonthlyStatEnums;
import cn.valuetodays.api2.module.fortune.client.persist.FundTradeMonthlyStatPO;
import cn.vt.util.HttpClient4Utils;
import cn.vt.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-30
 */
@Slf4j
@ApplicationScoped
public class SSEFundTradeMonthlyModule {
    private static final Double YI = 100000000.0D;

    /**
     * @param yearMonthStr yyy-MM  2023-05
     */
    public void request(final String yearMonthStr) {
        // http://www.sse.com.cn/market/funddata/overview/monthly/
        String url = "http://query.sse.com.cn/commonQuery.do"
            + "?"
            + "jsonCallBack=jsonpCallback96804386"
            + "&sqlId=COMMON_SSE_SJ_GPSJ_CJGK_MYGK_C"
            + "&PRODUCT_CODE=05%2C13%2C16%2C14%2C15%2C12"
            + "&SEARCH_DATE=" + yearMonthStr
            + "&type=inParams"
            + "&_=" + System.currentTimeMillis();
        String headersAsString = "Accept: */*\n"
            + "Accept-Encoding: gzip, deflate\n"
            + "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8\n"
            + "Connection: keep-alive\n"
            + "Cookie: ba17301551dcbaf9_gdp_user_key=; gdp_user_id=gioenc-0b77782g%2C89b0%2C5900%2Ca0d5%2C872eaa6c94d8; VISITED_MENU=%5B%228765%22%5D; ba17301551dcbaf9_gdp_session_id=58496e15-7e7c-4533-95fa-805fa539be2c; ba17301551dcbaf9_gdp_session_id_58496e15-7e7c-4533-95fa-805fa539be2c=true; ba17301551dcbaf9_gdp_sequence_ids={%22globalKey%22:101%2C%22VISIT%22:4%2C%22PAGE%22:14%2C%22VIEW_CLICK%22:71%2C%22VIEW_CHANGE%22:5%2C%22CUSTOM%22:11}\n"
            + "DNT: 1\n"
            + "Host: query.sse.com.cn\n"
            + "Referer: http://www.sse.com.cn/\n"
            + "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36";

        Map<String, String> headers = Arrays.stream(headersAsString.split("\n"))
            .collect(
                Collectors.toMap(
                    e -> e.split(":")[0].replace("#", ":"),
                    e -> StringUtils.trim(e.split(":")[1])
                )
            );

        String result = HttpClient4Utils.doGet(url, null, headers, null);
        parse(yearMonthStr, result);
    }

    public List<FundTradeMonthlyStatPO> requestHistory(final String yearMonthStr) {
        // http://www.sse.com.cn/market/funddata/overview/monthly/index_his.shtml
        String url = "http://query.sse.com.cn/commonQuery.do"
            + "?"
            + "jsonCallBack=jsonpCallback92617"
            + "&inYear=" + yearMonthStr
            + "&sqlId=COMMON_SSE_SJ_GPSJ_CJGK_MONTHCJGK_C"
            + "&fundType=47"
            + "&_=" + System.currentTimeMillis();
        String headersAsString = "Accept: */*\n"
            + "Accept-Encoding: gzip, deflate\n"
            + "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8\n"
            + "Connection: keep-alive\n"
            + "DNT: 1\n"
            + "Host: query.sse.com.cn\n"
            + "Referer: http://www.sse.com.cn/\n"
            + "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36";

        Map<String, String> headers = Arrays.stream(headersAsString.split("\n"))
            .collect(
                Collectors.toMap(
                    e -> e.split(":")[0].replace("#", ":"),
                    e -> StringUtils.trim(e.split(":")[1])
                )
            );

        String result = HttpClient4Utils.doGet(url, null, headers, null);
        return parse(yearMonthStr, result);
    }

    public List<FundTradeMonthlyStatPO> parse(final String yearMonthStr, String result) {
        int beginInx = result.indexOf("(");
        int endInx = result.lastIndexOf(")");
        String jsonObjStr = result.substring(beginInx + "(".length(), endInx);
        FundDataMonthlyResp resp = JsonUtils.fromJson(jsonObjStr, FundDataMonthlyResp.class);
        List<FundDataMonthlyData> dataList = resp.getResult();
        return dataList.stream().map(e -> {
            FundTradeMonthlyStatPO po = new FundTradeMonthlyStatPO();
            po.setCode(e.getCode());
            po.setName(e.getCode());
            po.setChannel(FundTradeMonthlyStatEnums.Channel.SSE);
            po.setType(FundTradeMonthlyStatEnums.Type.byCode(e.getCode()));
            po.setStatMonth(Integer.parseInt(StringUtils.remove(yearMonthStr, "-")));
            po.setGuaPaiNumber(e.getGuaPaiNumber());
            po.setBusiAmount(BigDecimal.valueOf(e.getBusiAmount()).multiply(BigDecimal.valueOf(YI)).longValue());
            po.setBusiMoney(BigDecimal.valueOf(e.getBusiMoney()).multiply(BigDecimal.valueOf(YI)).longValue());
            po.initUserIdAndTime(1L);
            return po;
        }).toList();
    }

    public List<FundTradeMonthlyStatPO> parseHistory(String yearMonthStr, String result) {
        int beginInx = result.indexOf("(");
        int endInx = result.lastIndexOf(")");
        String jsonObjStr = result.substring(beginInx + "(".length(), endInx);
        FundDataMonthlyHistoryResp resp = JsonUtils.fromJson(jsonObjStr, FundDataMonthlyHistoryResp.class);
        List<FundDataMonthlyHistoryData> dataList = resp.getResult();
        return dataList.stream()
            .distinct()
            .filter(e -> StringUtils.isNotBlank(e.getGuaPaiNumberStr()) && !StringUtils.equals(e.getGuaPaiNumberStr(), "-"))
            .map(e -> {
                FundTradeMonthlyStatPO po = new FundTradeMonthlyStatPO();
                po.setCode(e.getCode());
                po.setName(e.getCode());
                po.setChannel(FundTradeMonthlyStatEnums.Channel.SSE);
                po.setType(FundTradeMonthlyStatEnums.Type.byHistoryCode(e.getCode()));
                po.setStatMonth(Integer.parseInt(StringUtils.remove(yearMonthStr, "-")));
                po.setGuaPaiNumber(Integer.parseInt(e.getGuaPaiNumberStr()));
                po.setBusiAmount(BigDecimal.valueOf(e.getBusiAmount()).multiply(BigDecimal.valueOf(YI)).longValue());
                po.setBusiMoney(BigDecimal.valueOf(e.getBusiMoney()).multiply(BigDecimal.valueOf(YI)).longValue());
                po.initUserIdAndTime(1L);
                return po;
            }).toList();
    }

    @Data
    public static abstract class SseFundDataBaseResp<T> {
        private String isPagination; // "false",
        private String jsonCallBack; // "jsonpCallback71661925",
        private String locale; // "zh_CN",
        private T result;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FundDataMonthlyResp extends SseFundDataBaseResp<List<FundDataMonthlyData>> {

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FundDataMonthlyHistoryResp extends SseFundDataBaseResp<List<FundDataMonthlyHistoryData>> {

    }

    @Data
    public static class FundDataMonthlyData {
        @JsonProperty("PRODUCT_CODE")
        private String code;
        @JsonProperty("LIST_NUM")
        private int guaPaiNumber;
        @JsonProperty("TRADE_VOL")
        private Double busiAmount;
        @JsonProperty("TRADE_AMT")
        private Double busiMoney;
    }

    @Data
    public static class FundDataMonthlyHistoryData {
        @JsonProperty("PRODUCT_TYPE")
        private String code;
        @JsonProperty("TX_NUM")
        private String guaPaiNumberStr;
        @JsonProperty("TX_VOLUME")
        private Double busiAmount;
        @JsonProperty("TX_AMOUNT")
        private Double busiMoney;
    }

}
