package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.EtfT0DAO;
import cn.valuetodays.api2.module.fortune.dao.EtfT0DailyInfoDAO;
import cn.valuetodays.api2.module.fortune.persist.EtfT0DailyInfoPersist;
import cn.valuetodays.api2.module.fortune.persist.EtfT0PO;
import cn.valuetodays.api2.module.fortune.reqresp.T0DailyChartReq;
import cn.valuetodays.api2.module.fortune.reqresp.T0DailyChartResp;
import cn.valuetodays.api2.module.fortune.service.module.EtfT0DailyInfoModule;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.sse.SseEtfClientUtils;
import cn.vt.rest.third.sse.vo.TotalSharesResp;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * t0型etf每日数据
 *
 * @author lei.liu
 * @since 2025-01-04 11:44
 */
@ApplicationScoped
@Slf4j
public class EtfT0DailyInfoService
    extends BaseCrudService<Long, EtfT0DailyInfoPersist, EtfT0DailyInfoDAO> {
    @Inject
    EtfT0DAO etfT0DAO;
    @Inject
    EtfT0DailyInfoModule etfT0DailyInfoModule;
    @Inject
    private SqlServiceImpl sqlService;

    public int refresh() {
        List<EtfT0PO> etfT0s = etfT0DAO.listAll();
        int n = 0;
        for (EtfT0PO etfT0 : etfT0s) {
            n += etfT0DailyInfoModule.refreshOne(etfT0);
        }
        return n;
    }


    public void updateTotalSharesById(BigDecimal totalVolumeInWan, Long id) {
        getRepository().updateTotalSharesById(totalVolumeInWan, id);
    }

    /**
     *
     */
    public void fixTotalSharesInShBySql(Long lastId) {
        lastId = ObjectUtils.defaultIfNull(lastId, 0L);
        // 只查沪市
        final String sqlQuery = "select id, code, stat_date from fortune_etf_t0_daily_info "
            + " where code like '5%' and total_shares_wan < 0 and id > ? limit 200";
        final String sqlUpdate = "update fortune_etf_t0_daily_info set total_shares_wan=? where id=?";
        while (true) {
            log.info("lastId={}", lastId);
            List<EtfT0DailyInfoPersist> content = sqlService.queryForList(sqlQuery, EtfT0DailyInfoPersist.class, lastId);
            log.info("got {} records", content.size());
            if (CollectionUtils.isEmpty(content)) {
                break;
            }
            lastId = content.getLast().getId();
            List<Object[]> batchArgs = new ArrayList<>(content.size());
            for (EtfT0DailyInfoPersist one : content) {
                String code = one.getCode();
                Integer statDate = one.getStatDate();
                LocalDate localDate = DateUtils.formatYyyyMmDdAsLocalDateTime(statDate).toLocalDate();
                TotalSharesResp totalSharesResp = SseEtfClientUtils.getTotalVolumeInWan(code, localDate);
                List<TotalSharesResp.Item> result = totalSharesResp.getResult();
                if (CollectionUtils.isNotEmpty(result)) {
                    TotalSharesResp.Item item = result.getFirst();
                    BigDecimal totalVolumeInWan = item.getTotalVolumeInWan();
                    if (Objects.nonNull(totalVolumeInWan)) {
                        batchArgs.add(new Object[]{totalVolumeInWan, one.getId()});
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(batchArgs)) {
                sqlService.batchUpdate(sqlUpdate, batchArgs);
            }
        }
    }

    public List<EtfT0DailyInfoPersist> findAllByStatDate(Integer statDate) {
        return getRepository().findAllByStatDate(statDate);
    }


    public void fixTotalShares() {
        List<EtfT0PO> etfT0s = etfT0DAO.listAll();
        for (EtfT0PO etfT0 : etfT0s) {
            final String code = etfT0.getCode();
            // 先从集思录中获取数据，其中沪市的数据可能不准，再获取上证的数据覆盖一下
            doIgnoreException(() -> fixTotalSharesByCode(code));
            doIgnoreException(() -> etfT0DailyInfoModule.fillTotalSharesWan(LocalDate.now()));
        }
    }

    public void fixTotalSharesByCode(String code) {
        etfT0DailyInfoModule.fixTotalSharesByCode(code);
    }

    public List<T0DailyChartResp> dailyChart(T0DailyChartReq req) {
        String code = req.getCode();
        Set<T0DailyChartReq.MetricType> metricTypes = req.getMetricTypes();
        if (StringUtils.isBlank(code) || CollectionUtils.isEmpty(metricTypes)) {
            return List.of();
        }

        String partSql = metricTypes.stream()
            .distinct()
            .map(e -> "d." + e.getColumnName() + " as " + e.getColumnName())
            .collect(Collectors.joining(","));

        String sqlWithVar = """
            SELECT
                {{_PART_SQL_}},
                d.stat_date as statDate
            FROM `fortune_etf_t0_daily_info` d
            where
                code=?
            order by statDate asc
            """;
        sqlWithVar = StringUtils.replace(sqlWithVar, "{{_PART_SQL_}}", partSql);

        return sqlService.queryForList(sqlWithVar, T0DailyChartResp.class, code);
    }

}
