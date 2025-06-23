package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.persist.EtfT0DailyInfoPersist;
import cn.valuetodays.api2.module.fortune.client.reqresp.T0DailyChartReq;
import cn.valuetodays.api2.module.fortune.service.EtfT0DailyInfoService;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import cn.valuetodays.quarkus.commons.base.Operator;
import cn.valuetodays.quarkus.commons.base.PageQueryReqIO;
import cn.valuetodays.quarkus.commons.base.QuerySearch;
import cn.valuetodays.quarkus.commons.base.Sort;
import cn.vt.rest.third.sse.SseEtfClientUtils;
import cn.vt.rest.third.sse.vo.TotalSharesResp;
import cn.vt.util.DateUtils;
import cn.vt.web.RestPageImpl;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EtfT0DailyInfoService}.
 *
 * @author lei.liu
 * @since 2025-01-04
 */
//@EnabledOnOs(OS.WINDOWS)
@Slf4j
@QuarkusTest
public class EtfT0DailyInfoServiceTest {

    @Inject
    EtfT0DailyInfoService etfT0DailyInfoService;
    @Inject
    SqlServiceImpl jdbcTemplate;

    /**
     * 计算涨跌幅
     *
     * @param closePxBe closePxBe
     * @param closePxAf closePxAf
     * @return
     */
    private static BigDecimal calcOffset(BigDecimal closePxBe, BigDecimal closePxAf) {
        return closePxAf.subtract(closePxBe)
            .multiply(BigDecimal.valueOf(100))
            .divide(closePxAf, 4, RoundingMode.HALF_UP)
            ;
    }

    @Test
    public void checkOffsetBased924() {
        List<EtfT0DailyInfoPersist> befores = etfT0DailyInfoService.findAllByStatDate(20240924);
        List<EtfT0DailyInfoPersist> afters = etfT0DailyInfoService.findAllByStatDate(20250620);
        log.info("befores={}", befores);
        log.info("afters={}", afters);
        Map<String, EtfT0DailyInfoPersist> beforeMap = befores.stream()
            .collect(Collectors.toMap(EtfT0DailyInfoPersist::getCode, e -> e));
        for (EtfT0DailyInfoPersist af : afters) {
            EtfT0DailyInfoPersist be = beforeMap.get(af.getCode());
            if (ObjectUtils.allNotNull(be, af)) {
                BigDecimal closePxBe = be.getClosePx();
                BigDecimal closePxAf = af.getClosePx();
                BigDecimal offset = calcOffset(closePxBe, closePxAf);
                log.info("code={} offset={}%, closePxBe={}, closePxAf={}", af.getCode(), offset, closePxBe, closePxAf);
            }
        }
    }

    @Test
    void refresh() {
        etfT0DailyInfoService.refresh();
    }

    /**
     * 太慢了
     */
    @Test
    void fixTotalSharesByJpa() {
        Long lastId = 0L;
        PageQueryReqIO sortableQuerySearchIO = new PageQueryReqIO();
        sortableQuerySearchIO.setPageNum(1);
        sortableQuerySearchIO.setPageSize(100);
        sortableQuerySearchIO.setSorts(List.of(Sort.of(Sort.Direction.ASC, "id")));
        sortableQuerySearchIO.setSearches(List.of(QuerySearch.of("totalSharesWan", "-1", Operator.EQ)));
        while (true) {
            log.info("lastId={}", lastId);
            if (Objects.nonNull(lastId)) {
                sortableQuerySearchIO.getSearches().add(QuerySearch.of("id", Long.toString(lastId), Operator.GT));
            }
            RestPageImpl<EtfT0DailyInfoPersist> pagedData = etfT0DailyInfoService.query(sortableQuerySearchIO);
            List<EtfT0DailyInfoPersist> content = pagedData.getContent();
            if (CollectionUtils.isEmpty(content)) {
                break;
            }
            lastId = content.getLast().getId();
            for (EtfT0DailyInfoPersist one : content) {
                String code = one.getCode();
                Integer statDate = one.getStatDate();
                LocalDate localDate = DateUtils.formatYyyyMmDdAsLocalDateTime(statDate).toLocalDate();
                TotalSharesResp totalSharesResp = SseEtfClientUtils.getTotalVolumeInWan(code, localDate);
                List<TotalSharesResp.Item> result = totalSharesResp.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    TotalSharesResp.Item item = result.get(0);
                    BigDecimal totalVolumeInWan = item.getTotalVolumeInWan();
                    if (Objects.nonNull(totalVolumeInWan)) {
                        etfT0DailyInfoService.updateTotalSharesById(totalVolumeInWan, one.getId());
                    }
                }
            }
        }
    }

    /**
     *
     */
    @Test
    void fixTotalSharesInShBySql() {
        Long lastId = 0L;
        etfT0DailyInfoService.fixTotalSharesInShBySql(lastId);
    }


    @Test
    void fixTotalSharesBySql() {
        etfT0DailyInfoService.fixTotalShares();
    }

    @Test
    void updateByCode() {
        String code = "518600";
        etfT0DailyInfoService.fixTotalSharesByCode(code);
    }

    @Test
    void dailyChart() {
        T0DailyChartReq req = new T0DailyChartReq();
        req.setCode("518880");
        req.setMetricTypes(new HashSet<>(List.of(T0DailyChartReq.MetricType.CLOSE_PX, T0DailyChartReq.MetricType.OPEN_PX)));
        etfT0DailyInfoService.dailyChart(req);
    }
}
