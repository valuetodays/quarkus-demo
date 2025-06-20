package cn.valuetodays.api2.web.module.fortune.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.persist.EtfT0DailyInfoPersist;
import cn.valuetodays.api2.module.fortune.reqresp.T0DailyChartReq;
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
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests for {@link EtfT0DailyInfoService}.
 *
 * @author lei.liu
 * @since 2025-01-04
 */
@EnabledOnOs(OS.WINDOWS)
@Slf4j
public class EtfT0DailyInfoServiceTest {

    @Inject
    private EtfT0DailyInfoService etfT0DailyInfoService;
    @Inject
    private SqlServiceImpl jdbcTemplate;

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
