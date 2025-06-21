package cn.valuetodays.api2.module.fortune.service.module;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.persist.QuoteConstituentPO;
import cn.vt.util.DateUtils;
import cn.vt.util.HttpClient4Utils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-16
 */
@ApplicationScoped
@Slf4j
public class SinaQuoteConstituentModule {
    // totalPage的最后一页值
    private static final Integer LAST_VALUE_FOR_TOTAL_PAGE = -1;

    public List<QuoteConstituentPO> computeHistory(String quoteCode) {
        String urlTpl = "http://vip.stock.finance.sina.com.cn/corp/go.php/vII_HistoryComponent/indexid/000903.phtml";
        final String url = StringUtils.replace(urlTpl, "{{code}}", quoteCode);
        String s = HttpClient4Utils.doGet(url);
//        log.info("s={}", s);
        Document document = Jsoup.parse(s);
        Elements trList = document.select("#NewStockTable > tbody > tr");

        List<QuoteConstituentPO> collect = trList.stream().map(e -> {
                Elements tdList = e.select("td");
                String code = tdList.get(0).text();
                String name = tdList.get(1).text();
                String inDateStr = tdList.get(2).text();
                String outDateStr = tdList.get(3).text();

                if (!StringUtils.isNumeric(code)) {
                    return null;
                }

                QuoteConstituentPO qc = new QuoteConstituentPO();
                qc.setCode(code);
                qc.setName(name);
                LocalDate inDate = (parseIfNecessary(inDateStr));
                LocalDate outDate = (parseIfNecessary(outDateStr));
                log.info("inDate={}, outDate={}", inDate, outDate);
                return qc;
            })
            .filter(Objects::nonNull)
            .toList();
        log.info("s");
        return collect;
    }

    private LocalDate parseIfNecessary(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        if (StringUtils.startsWith(dateStr, "-")) {
            return null;
        }
        return LocalDate.parse(dateStr, DateUtils.DEFAULT_DATE_FORMATTER);
    }


    private Triple<List<QuoteConstituentPO>, Integer, Integer> computeNewest(String quoteCode, int page) {
        final String urlTpl = "https://vip.stock.finance.sina.com.cn/corp/view/vII_NewestComponent.php"
            + "?"
            + "{{pageParam}}"
            + "&indexid={{code}}";
        String url = urlTpl;
        url = StringUtils.replace(url, "{{code}}", quoteCode);
        if (page == -1) {
            url = StringUtils.replace(url, "{{pageParam}}", StringUtils.EMPTY);
        } else {
            url = StringUtils.replace(url, "{{pageParam}}", "page=" + page);
        }
        String s = HttpClient4Utils.doGet(url);
//        log.info("s={}", s);
        Document document = Jsoup.parse(s);
        Elements trList = document.select("#NewStockTable > tbody > tr");
        Elements pageElement = document.select("#NewStockTable + table");
        // ↑返回页顶↑
        // 当前页面是第1页,共8页 第一页|上一页| 下一页| 尾页  ↑返回页顶↑
        int currentPage = -1;
        int totalPage = LAST_VALUE_FOR_TOTAL_PAGE;
        if (Objects.nonNull(pageElement)) {
            String elementAsText = pageElement.text();
            if (StringUtils.isNotBlank(elementAsText)) {
                String currentPagePrefixText = "当前页面是第";
                if (elementAsText.contains(currentPagePrefixText)) {
                    String currentPageFullText = elementAsText.substring(
                        elementAsText.indexOf(currentPagePrefixText) + currentPagePrefixText.length()
                    );
                    String currentPageText = currentPageFullText.substring(0, 1);
                    currentPage = Integer.parseInt(currentPageText);

                    String totalPagePrefixText = "页,共";
                    String totalPageFullText = elementAsText.substring(
                        elementAsText.indexOf(totalPagePrefixText) + totalPagePrefixText.length()
                    );
                    String totalPageText = totalPageFullText.substring(0, 1);
                    totalPage = Integer.parseInt(totalPageText);
                }
            }
        }

        List<QuoteConstituentPO> list = trList.stream().map(e -> {
                Elements tdList = e.select("td");
                String code = tdList.get(0).text();
                String name = tdList.get(1).text();
                String inDateStr = tdList.get(2).text();

                if (!StringUtils.isNumeric(code)) {
                    return null;
                }

                QuoteConstituentPO qc = new QuoteConstituentPO();
                qc.setCode(code);
                qc.setName(name);
                LocalDate inDate = (parseIfNecessary(inDateStr));
                log.info("inDate={}", inDate);
                return qc;
            })
            .filter(Objects::nonNull)
            .toList();
        log.info("s");

        return Triple.of(list, currentPage, totalPage);
    }

    public List<QuoteConstituentPO> computeNewest(String quoteCode) {
        Triple<List<QuoteConstituentPO>, Integer, Integer> listAndPage = computeNewest(quoteCode, -1);
        List<QuoteConstituentPO> listInFirstPage = listAndPage.getLeft();
        int currentPage = listAndPage.getMiddle();
        int totalPage = listAndPage.getRight();

        if (isLastPage(totalPage)) {
            return listInFirstPage;
        } else {
            List<QuoteConstituentPO> full = new ArrayList<>(listInFirstPage.size() * totalPage);
            full.addAll(listInFirstPage);
            for (int i = currentPage + 1; i <= totalPage; i++) {
                Triple<List<QuoteConstituentPO>, Integer, Integer> objects = computeNewest(quoteCode, i);
                full.addAll(objects.getLeft());
            }
            return full;
        }
    }

    private boolean isLastPage(Integer totalPage) {
        return LAST_VALUE_FOR_TOTAL_PAGE.equals(totalPage);
    }

}
