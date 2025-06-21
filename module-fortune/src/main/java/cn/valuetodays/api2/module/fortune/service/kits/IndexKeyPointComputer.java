package cn.valuetodays.api2.module.fortune.service.kits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.persist.QuoteDailyStatPO;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 计算关键点位.
 *
 * @author lei.liu
 * @since 2023-04-05 22:32
 */
public final class IndexKeyPointComputer {

    private double offsetPercentage;

    public IndexKeyPointComputer() {
    }

    public List<QuoteDailyStatPO> doCompute(List<QuoteDailyStatPO> list, double offsetPercentage) {
        final List<QuoteDailyStatPO> highList = new ArrayList<>();
        final List<QuoteDailyStatPO> lowList = new ArrayList<>();
        this.offsetPercentage = offsetPercentage;

        List<QuoteDailyStatPO> sortedList = list.stream()
            .sorted(Comparator.comparingInt(QuoteDailyStatPO::getStatDate))
            .toList();

        tryParseHigh(sortedList, null, highList, lowList);
        return getKeyPointListOrderByMinTimeAsc(highList, lowList);
    }

    private List<QuoteDailyStatPO> getKeyPointListOrderByMinTimeAsc(List<QuoteDailyStatPO> highList,
                                                                    List<QuoteDailyStatPO> lowList) {
        List<QuoteDailyStatPO> totalList = new ArrayList<>();
        totalList.addAll(highList);
        totalList.addAll(lowList);
        return totalList.stream()
            .sorted(Comparator.comparingInt(QuoteDailyStatPO::getStatDate))
            .toList();
    }

    private void tryParseLow(List<QuoteDailyStatPO> readOnlyList,
                             QuoteDailyStatPO highest,
                             List<QuoteDailyStatPO> highList,
                             List<QuoteDailyStatPO> lowList) {
        if (CollectionUtils.isEmpty(readOnlyList)) {
            return;
        }
        QuoteDailyStatPO lowest = parseLowest(readOnlyList);
        lowList.add(lowest);
        //        List<IndexDailyStatPO> list = readOnlyList;
        List<QuoteDailyStatPO> list = removeMarginAroundLowest(readOnlyList, lowest);


        Predicate<QuoteDailyStatPO> leftPredicate;
        Predicate<QuoteDailyStatPO> rightPredicate;
        // lowest 在 highest 的左边
        if (lowest.getStatDate() < highest.getStatDate()) {
            leftPredicate = e -> e.getStatDate() < lowest.getStatDate();
            rightPredicate = e -> e.getStatDate() > lowest.getStatDate() && e.getStatDate() < highest.getStatDate();
        } else {
            // lowest 在 highest 的右边
            leftPredicate = e -> e.getStatDate() < lowest.getStatDate() && e.getStatDate() > highest.getStatDate();
            rightPredicate = e -> e.getStatDate() > lowest.getStatDate();
        }
        List<QuoteDailyStatPO> leftList = list.stream().filter(leftPredicate).toList();
        List<QuoteDailyStatPO> rightList = list.stream().filter(rightPredicate).toList();
        tryParseHigh(leftList, lowest, highList, lowList);
        tryParseHigh(rightList, lowest, highList, lowList);
    }


    private void tryParseHigh(List<QuoteDailyStatPO> readOnlyList,
                              QuoteDailyStatPO lowest,
                              List<QuoteDailyStatPO> highList,
                              List<QuoteDailyStatPO> lowList) {
        if (CollectionUtils.isEmpty(readOnlyList)) {
            return;
        }
        QuoteDailyStatPO highest = parseHighest(readOnlyList);
        highList.add(highest);
//        List<IndexDailyStatPO> list = readOnlyList;
        List<QuoteDailyStatPO> list = removeMarginAroundHighest(readOnlyList, highest);

        if (Objects.isNull(lowest)) {
            // 取出左和右，并把high去除掉
            List<QuoteDailyStatPO> leftList = list.stream()
                .filter(e -> e.getStatDate() < highest.getStatDate())
                .toList();
            List<QuoteDailyStatPO> rightList = list.stream()
                .filter(e -> e.getStatDate() > highest.getStatDate())
                .toList();
            tryParseLow(leftList, highest, highList, lowList);
            tryParseLow(rightList, highest, highList, lowList);
        } else {
            Predicate<QuoteDailyStatPO> leftPredicate;
            Predicate<QuoteDailyStatPO> rightPredicate;
            Integer minTimeInLowest = lowest.getStatDate();
            // lowest 在 highest 的左边
            if (minTimeInLowest < highest.getStatDate()) {
                leftPredicate = e -> e.getStatDate() < highest.getStatDate() && e.getStatDate() > minTimeInLowest;
                rightPredicate = e -> e.getStatDate() > highest.getStatDate();
            } else {
                // lowest 在 highest 的右边
                leftPredicate = e -> e.getStatDate() < highest.getStatDate();
                rightPredicate = e -> e.getStatDate() > highest.getStatDate() && e.getStatDate() < minTimeInLowest;
            }

            List<QuoteDailyStatPO> leftList = list.stream().filter(leftPredicate).toList();
            List<QuoteDailyStatPO> rightList = list.stream().filter(rightPredicate).toList();
            tryParseLow(leftList, highest, highList, lowList);
            tryParseLow(rightList, highest, highList, lowList);
        }
    }

    private List<QuoteDailyStatPO> removeMarginAroundHighest(List<QuoteDailyStatPO> list, QuoteDailyStatPO highest) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }

        int index = list.indexOf(highest);
        if (index <= 0) {
            return list;
        }
        QuoteDailyStatPO preHighest = list.get(index - 1);
        for (int i = index - 1; i > 0; i--) {
            QuoteDailyStatPO po = list.get(i);
            Double highPx = po.getHighPx();
            if (preHighest.getHighPx().compareTo(highPx) < 0 && isPercentageGreaterThan(highest.getHighPx(), po.getHighPx())) {
                break;
            } else {
                preHighest = po;
                list.set(i, null);
            }
        }
        if (index < list.size() - 1) {
            QuoteDailyStatPO postHighest = list.get(index + 1);
            for (int i = index + 1; i < list.size(); i++) {
                QuoteDailyStatPO po = list.get(i);
                Double highPx = po.getHighPx();
                if (postHighest.getHighPx().compareTo(highPx) < 0 && isPercentageGreaterThan(highest.getHighPx(), po.getHighPx())) {
                    break;
                } else {
                    postHighest = po;
                    list.set(i, null);
                }
            }
        }
        return list.stream().filter(Objects::nonNull).toList();
    }

    private List<QuoteDailyStatPO> removeMarginAroundLowest(List<QuoteDailyStatPO> list, QuoteDailyStatPO lowest) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }

        int index = list.indexOf(lowest);
        if (index <= 0) {
            return list;
        }
        QuoteDailyStatPO preLowest = list.get(index - 1);
        for (int i = index - 1; i > 0; i--) {
            QuoteDailyStatPO po = list.get(i);
            Double lowPx = po.getLowPx();
            if (preLowest.getLowPx().compareTo(lowPx) > 0 && isPercentageGreaterThan(po.getLowPx(), lowest.getLowPx())) {
                break;
            } else {
                preLowest = po;
                list.set(i, null);
            }
        }
        if (index < list.size() - 1) {
            QuoteDailyStatPO postLowest = list.get(index + 1);
            for (int i = index + 1; i < list.size(); i++) {
                QuoteDailyStatPO po = list.get(i);
                Double lowPx = po.getLowPx();
                if (postLowest.getLowPx().compareTo(lowPx) > 0 && isPercentageGreaterThan(po.getLowPx(), lowest.getLowPx())) {
                    break;
                } else {
                    postLowest = po;
                    list.set(i, null);
                }
            }
        }
        return list.stream().filter(Objects::nonNull).toList();
    }

    private boolean isPercentageGreaterThan(double highValue,
                                            double lowValue) {
        double offsetValue = highValue - lowValue;
        double offsetPercentage = offsetValue / highValue;
        return BigDecimal.valueOf(offsetPercentage).compareTo(BigDecimal.valueOf(this.offsetPercentage)) >= 0;
    }

    private QuoteDailyStatPO parseHighest(List<QuoteDailyStatPO> list) {
        return parseFirstByComparator(
            list,
            Comparator.comparingDouble(QuoteDailyStatPO::getHighPx).reversed()
        );
    }

    private QuoteDailyStatPO parseLowest(List<QuoteDailyStatPO> list) {
        return parseFirstByComparator(
            list,
            Comparator.comparingDouble(QuoteDailyStatPO::getLowPx)
        );
    }

    private <T> T parseFirstByComparator(List<T> list, Comparator<T> comparator) {
        return list.stream()
            .sorted(comparator)
            .collect(Collectors.toList())
            .get(0);
    }

}
