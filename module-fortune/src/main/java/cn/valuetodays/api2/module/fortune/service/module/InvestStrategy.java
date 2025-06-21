package cn.valuetodays.api2.module.fortune.service.module;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import cn.vt.util.DateUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-05 10:44
 */
public interface InvestStrategy {
    static int toYyyyMMdd(LocalDate localDate) {
        String yyyyMMddStr = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return Integer.parseInt(yyyyMMddStr);
    }

    static LocalDate fromYyyyMMdd(int yyyyMMdd) {
        LocalDateTime ldt = DateUtils.getDate(String.valueOf(yyyyMMdd), "yyyyMMdd");
        return ldt.toLocalDate();
    }

    static List<QuoteDailyStatPO> computeTradeDays(InvestStrategyReq req, List<QuoteDailyStatPO> dataList) {
        LocalDate beginDate = req.getBeginDate();
        LocalDate endDate = req.getEndDate();

        int beginDateAsInt = InvestStrategy.toYyyyMMdd(beginDate);
        int endDateAsInt = InvestStrategy.toYyyyMMdd(endDate);
        List<QuoteDailyStatPO> tradeDays = new ArrayList<>();
        Map<Integer, QuoteDailyStatPO> statDateAndObjMap = dataList.stream()
            .collect(Collectors.toMap(QuoteDailyStatPO::getStatDate, e -> e));
        List<Integer> minTimeAscList = statDateAndObjMap.keySet().stream()
            .filter(e -> beginDateAsInt <= e && endDateAsInt >= e)
            .sorted()
            .toList();
        final int firstDayOfMonth = 1;

        while (beginDate.isBefore(endDate)) {
            if (beginDate.getDayOfMonth() == firstDayOfMonth) {
                int i;
                LocalDate tmp = beginDate;
                while (true) {
                    int yyyyMMdd = InvestStrategy.toYyyyMMdd(tmp);
                    i = minTimeAscList.indexOf(yyyyMMdd);
                    if (i > -1) {
                        break;
                    }
                    tmp = tmp.plusDays(1);
                }
                tradeDays.add(statDateAndObjMap.get(minTimeAscList.get(i)));
            }
            beginDate = beginDate.plusMonths(1).withDayOfMonth(firstDayOfMonth);
        }
        return tradeDays;
    }

    InvestStrategyResp doInvest(InvestStrategyReq req);
}
