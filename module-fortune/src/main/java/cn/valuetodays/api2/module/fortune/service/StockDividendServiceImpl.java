package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.StockDividendDAO;
import cn.valuetodays.api2.module.fortune.persist.StockDividendPO;
import cn.valuetodays.api2.module.fortune.service.module.SinaStockDividendModule;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-08-18
 */
@ApplicationScoped
@Slf4j
public class StockDividendServiceImpl
    extends BaseCrudService<Long, StockDividendPO, StockDividendDAO> {
    @Inject
    SinaStockDividendModule sinaStockDividendModule;

    public void parseAndSave(String code) {
        if (StringUtils.isBlank(code)) {
            return;
        }
        List<StockDividendPO> listToSave = sinaStockDividendModule.visitHtmlPageAndParse(code);
        if (CollectionUtils.isEmpty(listToSave)) {
            return;
        }
        List<StockDividendPO> listInDb = getRepository().findAllByCode(code);
        if (CollectionUtils.isEmpty(listInDb)) {
            this.save(listToSave);
            return;
        }
        final Map<LocalDate, StockDividendPO> statDateAndPOMap = listInDb.stream()
            .collect(Collectors.toMap(StockDividendPO::getStatDate, e -> e));
        List<StockDividendPO> finalListToSave = listToSave.stream()
            .peek(e -> {
                StockDividendPO po = statDateAndPOMap.get(e.getStatDate());
                if (Objects.nonNull(po)) {
                    e.setId(po.getId());
                }
            })
            .filter(e -> Objects.isNull(e.getId()))
            .toList();
        if (CollectionUtils.isNotEmpty(finalListToSave)) {
            this.save(finalListToSave);
        }

    }

    public void parseAndSave(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return;
        }
        for (String code : codes) {
            parseAndSave(code);
        }
    }

    public List<StockDividendPO> listLastDividends(List<String> codes) {
        final LocalDate firstDayOfLastYear = LocalDate.now().plusYears(-1).withDayOfYear(1);
        final LocalDate firstDayOfThisYear = LocalDate.now().withDayOfYear(1);
        List<StockDividendPO> list = getRepository().findAllByCodeInAndStatDateGreaterThan(codes, firstDayOfLastYear);
        Map<String, List<StockDividendPO>> map = list.stream().collect(Collectors.groupingBy(StockDividendPO::getCode));
        return map.values().stream().map(value -> {
            if (CollectionUtils.size(value) == 1) {
                return value.get(0);
            } else {
                StockDividendPO first = value.get(0);
                // 优先取今年的数据，今年没有再取去年的，注意是只取今年或去年的
                // 有些股票会每年分红多次
                List<StockDividendPO> dividendListForThisYear = value.stream()
                    .filter(e -> e.getStatDate().isAfter(firstDayOfThisYear))
                    .toList();
                if (CollectionUtils.isNotEmpty(dividendListForThisYear)) {
                    first.setPaiPerTen(parsePaiPerTen(dividendListForThisYear));
                } else {
                    first.setPaiPerTen(parsePaiPerTen(value));
                }
                return first;
            }
        }).toList();
    }

    private BigDecimal parsePaiPerTen(List<StockDividendPO> list) {
        return list.stream()
            .map(StockDividendPO::getPaiPerTen)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }
}
