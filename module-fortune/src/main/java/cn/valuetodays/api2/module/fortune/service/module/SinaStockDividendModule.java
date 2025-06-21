package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.StockDividendPO;
import cn.vt.util.DateUtils;
import cn.vt.util.HttpClient4Utils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-18
 */
@ApplicationScoped
@Slf4j
public class SinaStockDividendModule {
    /**
     * @param code 601328
     * @return
     */
    public List<StockDividendPO> visitHtmlPageAndParse(final String code) {
        final String urlTpl = "https://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/{{CODE}}.phtml";
        final String url = StringUtils.replace(urlTpl, "{{CODE}}", code);
        String pageSource = HttpClient4Utils.doGet(url, null, "gb2312");
        if (StringUtils.contains(pageSource, "拒绝访问")) {
            return new ArrayList<>(0);
        }

        Document document = Jsoup.parse(pageSource);
        Elements trList = document.select("table#sharebonus_1 > tbody > tr");
        List<StockDividendPO> rawList = trList.stream()
            .map(trEle -> {
                Elements tdList = trEle.select("td");
                String songText = tdList.get(1).text();
                String zhuanText = tdList.get(2).text();
                String paiText = tdList.get(3).text();
                String statusText = tdList.get(4).text();
                String dateStr = tdList.get(5).text();
                if (!"实施".equals(statusText)) {
                    return null;
                }
                StockDividendPO sd = new StockDividendPO();
                sd.setCode(code);
                try {
                    // 无效数据的处理
                    if (StringUtils.contains(dateStr, "--")) {
                        dateStr = tdList.get(7).text();
                    }
                    sd.setStatDate(DateUtils.getDate(dateStr).toLocalDate());
                } catch (Exception e) {
                    log.error("error at {} {}", url, dateStr, e);
                }
                sd.setSongPerTen(new BigDecimal(songText));
                sd.setZhuanPerTen(new BigDecimal(zhuanText));
                sd.setPaiPerTen(new BigDecimal(paiText));
                sd.initUserIdAndTime(1L);
                return sd;
            })
            .filter(Objects::nonNull)
            .toList();

        // merge by statDate
        Map<LocalDate, List<StockDividendPO>> mapList = rawList.stream()
            .collect(Collectors.groupingBy(StockDividendPO::getStatDate));

        return mapList.entrySet().stream()
            .map(e -> {
                LocalDate key = e.getKey();
                List<StockDividendPO> value = e.getValue();
                int size = CollectionUtils.size(value);
                if (size == 1) {
                    return value.get(0);
                }
                BigDecimal addedSong = value.stream()
                    .map(StockDividendPO::getSongPerTen)
                    .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal addedZhuan = value.stream()
                    .map(StockDividendPO::getZhuanPerTen)
                    .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal addedPai = value.stream()
                    .map(StockDividendPO::getPaiPerTen)
                    .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                StockDividendPO toUse = value.get(0);
                toUse.setSongPerTen(addedSong);
                toUse.setZhuanPerTen(addedZhuan);
                toUse.setPaiPerTen(addedPai);
                return toUse;
            }).toList();
    }
}
