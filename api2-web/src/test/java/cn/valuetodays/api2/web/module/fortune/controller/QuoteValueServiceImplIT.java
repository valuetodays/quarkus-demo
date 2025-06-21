package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.QuoteValuePO;
import cn.valuetodays.api2.module.fortune.service.QuoteValueServiceImpl;
import cn.vt.rest.third.danjuan.vo.DjIndexData;
import cn.vt.rest.third.danjuan.vo.DjPbData;
import cn.vt.util.DateUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-08-17 17:45
 */
@Slf4j
class QuoteValueServiceImplIT {

    @Inject
    QuoteValueServiceImpl quoteValueService;


    @Test
    void refresh() {
        quoteValueService.refresh();
    }

    @Test
    public void fixPb() {
        List<DjIndexData.Item> allIndexObjs = quoteValueService.getAllIndexObjs();
        for (DjIndexData.Item indexObj : allIndexObjs) {
            String regionAndCode = indexObj.getIndexCode();
            String region = regionAndCode.substring(0, 2);
            String code = regionAndCode.substring(2);
            String name = indexObj.getName();
            if (!Arrays.asList("SH", "SZ").contains(region)) {
                continue;
            }
            List<DjPbData.IndexEvaPbGrowth> pbList = quoteValueService.gatherPb(indexObj, region, code);
            Map<Integer, Double> tsAndPbMap = pbList.stream()
                .collect(Collectors.toMap(e -> DateUtils.formatAsYyyyMMdd(e.getTs()), DjPbData.IndexEvaPbGrowth::getPb));
            List<QuoteValuePO> valueList = quoteValueService.findAllByCode(code);
            valueList.forEach(e -> e.setPbVal(tsAndPbMap.getOrDefault(e.getStatDate(), 0d)));
            quoteValueService.save(valueList);
        }
    }

    /**
     * 接口上只能获取最近10年的pe列表
     * 操作建议：
     * pe百分位 > 70 卖出
     * pe百分位 > 30 持有
     * pe百分位 > 0  买入
     * <p>
     * 指数估值数据说明
     * PE：市盈率，指数成分股总市值与总利润的比率。通俗来讲就是按照当年总利润计算，投资需要多少年才可以收回成本。
     * 百分位：当前数值在历史数值中所处的位置。
     * PB：市净率，每股股价与每股净资产的比率。通常用市净率来评估的公司，公司的营业收入和利润对资产的依赖较大，例如资源类、制造业企业尤其适合用市净率。
     * 股息率：是一年的总派息额与当时市价的比例，衡量企业分红能力的指标。
     * ROE：净资产收益率，是企业税后利润除以净资产得到的百分比率，衡量盈利能力的指标。该指标越高，说明投资带来的收益越高。
     * 预测PEG：市盈率除以盈利增长的比率，是考虑了利润增长之后的估值指标。
     */
    @Test
    void printPePercentage() {
//        String code = "000300";
        String code = "000905";
        List<QuoteValuePO> list = quoteValueService.findAllByCode(code);
        List<QuoteValuePO> sortedList = list.stream()
            .sorted(Comparator.comparingInt(QuoteValuePO::getStatDate).reversed())
            .toList();
        for (int i = 0; i < sortedList.size() - 2; i++) {
            QuoteValuePO QuoteValuePO = sortedList.get(i);
            List<QuoteValuePO> previousPeList = sortedList.subList(i + 1, sortedList.size() - 1);
            double percentage = calcPercentage(QuoteValuePO, previousPeList);
            String suggestion = formatSuggestion(percentage);
            log.info("date: {}, percentage: {}, suggestion: {}",
                QuoteValuePO.getStatDate(), percentage, suggestion);
        }
    }

    private String formatSuggestion(double percentage) {
        if (percentage > 70) {
            return "SELL";
        } else if (percentage > 30) {
            return "HOLD";
        }
        return "BUY";
    }

    private double calcPercentage(QuoteValuePO QUoteValuePO, List<QuoteValuePO> previousPeList) {
        Double val = QUoteValuePO.getPeVal();
        long count = previousPeList.stream().filter(e -> e.getPeVal() < val).count();
        return BigDecimal.valueOf(count * 100)
            .divide(BigDecimal.valueOf(previousPeList.size()), 4, RoundingMode.HALF_UP)
            .doubleValue();
    }

}
