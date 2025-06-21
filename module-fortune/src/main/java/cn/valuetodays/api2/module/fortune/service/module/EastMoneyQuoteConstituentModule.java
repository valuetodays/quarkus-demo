
package cn.valuetodays.api2.module.fortune.service.module;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.QuoteConstituentDAO;
import cn.valuetodays.api2.module.fortune.dao.QuoteDAO;
import cn.valuetodays.api2.module.fortune.persist.QuoteConstituentPO;
import cn.valuetodays.api2.module.fortune.persist.QuotePO;
import cn.vt.core.Title;
import cn.vt.core.TitleCapable;
import cn.vt.rest.third.eastmoney.vo.EastmoneyBasePageResp;
import cn.vt.rest.third.eastmoney.vo.PagedData;
import cn.vt.spider.BaseApiSpider;
import cn.vt.spider.VisitPageSourceCallback;
import cn.vt.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-17
 */
@ApplicationScoped
@Slf4j
public class EastMoneyQuoteConstituentModule extends BaseApiSpider {
    /**
     * 网页的url
     */
    private static final String PAGE_URL = "https://data.eastmoney.com/other/index";
    /**
     * 网页中的某个接口的url
     */
    private static final String API_URL_TPL = "https://datacenter-web.eastmoney.com/api/data/v1/get"
        + "?"
        + "callback=jQuery112307799337965058148_1692243051285"
        + "&sortColumns=SECURITY_CODE"
        + "&sortTypes=-1"
        + "&pageSize={{PAGE}}"
        + "&pageNumber=1"
        + "&reportName=RPT_INDEX_TS_COMPONENT"
        + "&columns=SECUCODE%2CSECURITY_CODE%2CTYPE%2CSECURITY_NAME_ABBR%2CCLOSE_PRICE%2CINDUSTRY%2CREGION%2CWEIGHT%2CEPS%2CBPS%2CROE%2CTOTAL_SHARES%2CFREE_SHARES%2CFREE_CAP"
        + "&quoteColumns=f2%2Cf3"
        + "&quoteType=0"
        + "&source=WEB"
        + "&client=WEB"
        + "&filter=(TYPE%3D%22{{FILTER_VALUE}}%22)";
    @Inject
    QuoteConstituentDAO quoteConstituentDAO;
    @Inject
    QuoteDAO quoteDAO;

    public void scheduleGather() {
        final String[] resultStr = new String[1];
        for (Type type : Type.values()) {
            String apiUrl = formatApiUrl(type);
            super.doVisitPageSource(apiUrl,
                StandardCharsets.UTF_8,
                new HashMap<>(),
                new VisitPageSourceCallback<Integer, Object>() {
                    @Override
                    public Integer callback(String url, String pageSource, Object obj) {
//                        log.info("pageSource={}", pageSource);
                        resultStr[0] = pageSource;
                        return 0;
                    }
                }, null
            );

            QuotePO quotePO = quoteDAO.findByCode(type.getCode());
            if (Objects.isNull(quotePO)) {
                log.warn("no quote with code={}", type.getCode());
                continue;
            }
            parseResultAndSaveToDb(resultStr[0], quotePO);
        }
    }

    private String formatApiUrl(Type type) {
        String url = API_URL_TPL;
        url = StringUtils.replace(url, "{{PAGE}}", String.valueOf(type.getItemCount()));
        url = StringUtils.replace(url, "{{FILTER_VALUE}}", type.getFilterValue());
        return url;
    }

    private void parseResultAndSaveToDb(String resultString, QuotePO quotePO) {
        String jsonString = resultString.substring(resultString.indexOf("{"), resultString.length() - ");".length());
        log.info("jsonString={}", jsonString);
        EastMoneyQuoteConstituentDetailResp detailResp =
            JsonUtils.fromJson(jsonString, EastMoneyQuoteConstituentDetailResp.class);
        PagedData<EastMoneyQuoteConstituentData> result = detailResp.getResult();
        List<EastMoneyQuoteConstituentData> dataListInResp = result.getData();
        final LocalDate statDate = LocalDate.now();
        final Long createUserId = 1L;
        List<QuoteConstituentPO> listToSave = dataListInResp.stream()
            .map(e -> {
                QuoteConstituentPO detail = new QuoteConstituentPO();
                detail.setQuoteId(quotePO.getId());
                detail.setCode(e.getCode());
                detail.setName(e.getName());
                detail.setId(null);
                detail.setStatDate(statDate);
                detail.initUserIdAndTime(createUserId);
                return detail;
            })
            .toList();
        List<QuoteConstituentPO> listInDb = quoteConstituentDAO.findAllByQuoteIdAndStatDateAndCodeIn(
            quotePO.getId(),
            statDate,
            listToSave.stream()
                .map(QuoteConstituentPO::getCode)
                .distinct()
                .collect(Collectors.toList())
        );
        if (CollectionUtils.isEmpty(listInDb)) {
//            quoteConstituentDAO.saveAll(listToSave);
        } else {
            final Map<String, QuoteConstituentPO> codeAndPOMap = listInDb.stream()
                .collect(Collectors.toMap(QuoteConstituentPO::getCode, e -> e));
            List<QuoteConstituentPO> finalListToSave = listToSave.stream()
                .peek(e -> {
                    QuoteConstituentPO po = codeAndPOMap.get(e.getCode());
                    if (Objects.nonNull(po)) {
                        e.setId(po.getId());
                    }
                })
                .filter(e -> Objects.isNull(e.getId()))
                .toList();
            if (CollectionUtils.isNotEmpty(finalListToSave)) {
//                quoteConstituentDAO.saveAll(finalListToSave);
            }
        }
    }

    @Getter
    public enum Type implements TitleCapable {
        HS300("沪深300", "000300", 300, "1"),
        SZ50("上证50", "000016", 50, "2"),
        ZZ500("中证500", "000905", 500, "3"),
        KC50("科创50", "000688", 50, "4");

        private final String title;
        private final String code;
        private final int itemCount;
        private final String filterValue;

        Type(String title, String code, int itemCount, String filterValue) {
            this.title = title;
            this.code = code;
            this.itemCount = itemCount;
            this.filterValue = filterValue;
        }
    }

    @Data
    private static class EastMoneyQuoteConstituentData implements Serializable {
        @JsonProperty("SECURITY_CODE")
        @Title("股票代码")
        private String code;
        @Title("股票简称")
        @JsonProperty("SECURITY_NAME_ABBR")
        private String name;
        @JsonProperty("CLOSE_PRICE")
        @Title("最新价（元）")
        private double closePrice;
        @JsonProperty("INDUSTRY")
        @Title("主营行业")
        private String industry;
        @JsonProperty("REGION")
        @Title("地区")
        private String region;
        @JsonProperty("EPS")
        @Title("每股收益（元）")
        private double eps;
        @JsonProperty("BPS")
        @Title("每股净资产（元）")
        private double bps;
        @JsonProperty("ROE")
        @Title("净资产收益率（%）")
        private double roe;
        @JsonProperty("TOTAL_SHARES")
        @Title("总股本（亿股）")
        private double totalShares;
        @JsonProperty("FREE_SHARES")
        @Title("流通股本（亿股）")
        private double freeShares;
        @JsonProperty("FREE_CAP")
        @Title("流通市值（亿元）")
        private double freeCap;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class EastMoneyQuoteConstituentDetailResp
        extends EastmoneyBasePageResp<EastMoneyQuoteConstituentData>
        implements Serializable {

    }
}
