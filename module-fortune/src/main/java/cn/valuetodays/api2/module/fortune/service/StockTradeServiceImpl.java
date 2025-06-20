package cn.valuetodays.api2.module.fortune.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.channel.StockTradeLogParser;
import cn.valuetodays.api2.module.fortune.channel.StockTradeLogParserFactory;
import cn.valuetodays.api2.module.fortune.dao.StockDAO;
import cn.valuetodays.api2.module.fortune.dao.StockTradeDAO;
import cn.valuetodays.api2.module.fortune.persist.StockPO;
import cn.valuetodays.api2.module.fortune.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.reqresp.AnalyzeHedgeEarnedChartReq;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.moduled.fortune.StockConstants;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums.TradeType;
import cn.vt.util.DateUtils;
import cn.vt.util.StringExUtils;
import cn.vt.vo.NameValueVo;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-21
 */
@Slf4j
@ApplicationScoped
public class StockTradeServiceImpl
    extends BaseCrudService<Long, StockTradePO, StockTradeDAO> {

    @Inject
    StockDAO stockDAO;
    /*
    public AutoToHedgeTradeResp autoToHedge(int days) {
        AutoToHedgeTradeResp resp = new AutoToHedgeTradeResp();
        if (days <= 0 || days > 180) {
            resp.setMsg("illegal params: days=" + days);
            return resp;
        }

        Integer preTradeDate = minusDays(LocalDate.now(), days);
        List<StockTradePO> list = getRepository().findAllByHedgeIdAndTradeDateGreaterThan(0L, preTradeDate);
        StockTradeAutoHedger autoHedger = new StockTradeAutoHedger();
        autoHedger.doAutoHedge(list, resp);
        getRepository().saveAll(list);
        resp.setRecords(resp.getRecords());
        resp.setMatchedRecords(resp.getMatchedRecords());
        return resp;
    }
*/
    private static Integer minusDays(LocalDate localDate, int days) {
        // 取 trade_date > today - days 的时间
        LocalDate preTradeDateLd = localDate.minusDays(days);
        return DateUtils.formatAsYyyyMMdd(preTradeDateLd);
    }

    /**
     * 此方法不操作数据库
     *
     * @param trades trades
     */
 /*   public static AutoToHedgeTradeResp autoHedgeOrders(List<StockTradePO> trades) {
        AutoToHedgeTradeResp resp = new AutoToHedgeTradeResp();
        StockTradeAutoHedger autoHedger = new StockTradeAutoHedger();
        autoHedger.doAutoHedge(trades, resp);
        return resp;
    }
*/
/*
    public AutoToHedgeTradeResp tryAutoToHedge(String code, boolean dryRun) {
        AutoToHedgeTradeResp resp = new AutoToHedgeTradeResp();
        if (StringUtils.isBlank(code)) {
            resp.setMsg("illegal params: code=" + code);
            return resp;
        }
        List<StockTradePO> list = getRepository().findAllByCodeAndHedgeId(code, 0L);

        StockTradeAutoHedger autoHedger = new StockTradeAutoHedger();
        autoHedger.doAutoHedge(list, resp);
        if (!dryRun) {
            getRepository().saveAll(list);
        }
        return resp;
    }
*/
    /*
    public CheckHedgedKitsResp checkHedged(String code) {
        if (StringUtils.isBlank(code)) {
            return new CheckHedgedKitsResp();
        }
        List<StockTradePO> list = getRepository().findAllByCode(code);
        CheckHedgedKits kits = new CheckHedgedKits();
        return kits.doCheck(list);
    }
*/
    /*
    public SaveTradeMonitorByTradeResp saveTradeMonitorByTrade(SimpleTypesReq req) {
        List<Long> tradeIds = parseTextAsTradeIds(req);

        SaveTradeMonitorByTradeResp resp = new SaveTradeMonitorByTradeResp();
        if (CollectionUtils.isEmpty(tradeIds)) {
            resp.setMsg("tradeIds is empty/null");
            return resp;
        }
        List<StockTradePO> trades = this.list(tradeIds);
        List<String> resultList = new ArrayList<>();
        for (StockTradePO one : trades) {
            String result = "";
            if (Objects.isNull(one)) {
                result = "no entity";
            } else {
                Long hedgeId = one.getHedgeId();
                if (Objects.nonNull(hedgeId) && hedgeId > 0L) {
                    result = "hedged with " + hedgeId;
                }
                if (Objects.equals(hedgeId, 0L)) {
                    StockTradeMonitorPO monitorPO = new StockTradeMonitorPO();
                    monitorPO.setCode(one.getCode());
                    final boolean buyFlag = one.getTradeType().isBuyFlag();
                    monitorPO.setTradeType(TradeType.byBuyFlag(buyFlag).reversed());
                    monitorPO.setQuantity(one.getQuantity());
                    monitorPO.setPrice(PriceUtilsEx.calcOppositePrice(one));
                    monitorPO.setStatus(StockTradeMonitorEnums.Status.NORMAL);
                    String remark = (buyFlag ? "买价" : "卖价") + one.getPrice().doubleValue();
                    monitorPO.setRemark(remark);
                    monitorPO.setTradeId(one.getId());
                    monitorPO.initUserIdAndTime(one.getAccountId());
                    // 将StockTradePO#id与StockTradeMonitorPO#id保持一致
                    // setId()要在 initUserIdAndTime()之后
                    monitorPO.setId(one.getId());
                    try {
                        stockTradeMonitorDAO.save(monitorPO);
                        result = "OK";
                    } catch (Exception e) {
                        result = "error with " + e.getMessage();
                    }
                }
            }
            resultList.add(result);
        }
        resp.setResultList(resultList);
        return resp;
    }
*/
    private static List<Long> parseTextAsTradeIds(SimpleTypesReq req) {
        String tradeIdsStr = StringUtils.trimToEmpty(req.getText());

        String standardSep = ",";
        List<String> sepList = List.of("，", "\r\n", "\r", "\n");
        String replaced = StringExUtils.replaceAll(tradeIdsStr, sepList, standardSep);

        return Arrays.stream(replaced.split(standardSep))
            .map(StringUtils::trimToNull)
            .filter(Objects::nonNull)
            .map(Long::parseLong)
            .distinct()
            .toList();
    }

    /*
        public AffectedRowsResp saveByOrderInfos(List<DealedOrderInfo> dealedOrderInfos,
                                                 FortuneCommonEnums.Channel channel,
                                                 Long currentAccountId) {
            List<StockTradePO> listToSave = dealedOrderInfos.stream().map(e -> {
                    String code = StockCodeUtils.parseFromEhaifangzhou(e.getStock_code());
                    if (StockConstants.excludedCodes.contains(code)) {
                        return null;
                    }
                    StockTradePO stockTradePO = new StockTradePO();
                    TradeType tradeType = QmtConstants.TRADE_TYPE_MAP.get(e.getOrder_type());
                    AssertUtils.assertNotNull(tradeType, "order_type is wrong. " + e.getOrder_type());
                    LocalDateTime localDateTime = QmtUtils.orderTimeToLdt(e.getOrder_time());
                    LocalDate localDate = localDateTime.toLocalDate();
                    LocalTime localTime = localDateTime.toLocalTime();
                    Integer currentDate = DateUtils.formatAsYyyyMMdd(localDate);
                    final String uniqueId = QmtUtils.formatOrderUniqueId(e);
                    stockTradePO.setCode(code);
                    stockTradePO.setChannel(channel);
                    stockTradePO.setChannelOrderId(uniqueId);
                    stockTradePO.setTradeType(tradeType);
                    stockTradePO.setTradeDate(currentDate);
                    stockTradePO.setTradeTime(localTime);
                    stockTradePO.setQuantity(e.getTraded_volume());
                    stockTradePO.setPrice(e.getTraded_price());
                    stockTradePO.setYongjinFee(e.getYjFee());
                    stockTradePO.setYinhuaFee(e.getYhFee());
                    stockTradePO.setGuohuFee(e.getGhFee());
                    stockTradePO.setHedgeId(0L);
                    stockTradePO.setAccountId(currentAccountId);
                    return stockTradePO;
                })
                .filter(Objects::nonNull)
                .toList();

            List<StockTradePO> saved = this.save(listToSave);
            return AffectedRowsResp.of(saved.size());
        }
    */
    /*
    public AnalyzeHedgeEarnedChartResp analyzeHedgeEarnedChart(AnalyzeHedgeEarnedChartReq req) {
        final Tuple2<String, String> tuple2ForSql = buildSqlStatByDaily(req);
        final String sqlForTempTable = tuple2ForSql.getT1();
        final String sqlStatByDaily = tuple2ForSql.getT2();
        AnalyzeHedgeEarnedChartReq.HedgeEarnedType hedgeEarnedType = req.getHedgeEarnedType();

        jdbcTemplate.execute(sqlForTempTable);
        List<StatDailyDataVo> list = jdbcTemplate.query(
            sqlStatByDaily,
            new BeanPropertyRowMapper<>(StatDailyDataVo.class),
            req.getAccountId()
        );

        List<String> keysForDate;
        List<BigDecimal> valuesForDate;
        if (AnalyzeHedgeEarnedChartReq.HedgeEarnedType.MONTHLY == hedgeEarnedType) {
            Map<String, BigDecimal> finalMap = list.stream()
                .collect(
                    Collectors.toMap(
                        // 按月份聚合一下
                        e -> {
                            LocalDateTime ldt = DateUtils.formatYyyyMmDdAsLocalDateTime(e.getTradeDate());
                            return DateUtils.formatDate(ldt, "yyyy-MM");
                        },
                        StatDailyDataVo::getEarned,
                        BigDecimal::add
                    )
                );
            final List<String> tmpKeysForDate = new ArrayList<>();
            final List<BigDecimal> tmpValuesForDate = new ArrayList<>();
            finalMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(e -> {
                    tmpKeysForDate.add(e.getKey());
                    tmpValuesForDate.add(e.getValue());
                });
            keysForDate = tmpKeysForDate;
            valuesForDate = tmpValuesForDate;
        } else if (AnalyzeHedgeEarnedChartReq.HedgeEarnedType.DAILY == hedgeEarnedType) {
            keysForDate = list.stream()
                .map(e -> {
                    LocalDateTime ldt = DateUtils.formatYyyyMmDdAsLocalDateTime(e.getTradeDate());
                    return DateUtils.formatDate(ldt);
                })
                .toList();
            valuesForDate = list.stream().map(StatDailyDataVo::getEarned).toList();
        } else {
            throw new IllegalArgumentException("hedgeEarnedType must be MONTHLY or DAILY");
        }
        AnalyzeHedgeEarnedChartResp resp = new AnalyzeHedgeEarnedChartResp();
        resp.setDateList(keysForDate);
        resp.setEarnedList(valuesForDate);
        return resp;
    }
*/
    private static Pair<String, String> buildSqlStatByDaily(AnalyzeHedgeEarnedChartReq req) {
        /*
        此sql获取一笔交易及其对应的对冲交易（可能是多笔），
        但又得使盈利为正，
        ## 两笔交易即对冲的情况：
        若 10.08这天仅买入一笔 1000*1，第二天10.09这天仅卖出一笔 1000*1.001
        得到的数据是10.08这天亏了1000（忽略手续费），则第二天赚了10001（忽略手续费），
        而实际是10.08这天对冲盈利了0元，每二天对冲盈利了1元。
        所以在统计时将跨天交易组成的对冲交易全部按`最大成交日期`计算，（上面的例子中是，把10.08这笔交易算做10.09）
        这样就没有数据波动过大的情况
        ## 三笔及以上交易即对冲的情况：
        需要找到所有对冲交易中的id，然后获取`它们的最大交易时期`，使用一条sql实现不了，就先获取到这些数据（注意，只有少量数据是三笔及以上对冲的）
        然后需要判断最大时期时从该临时表中判断，获取不了就从当前交易表的trade_date及其hedge_id对应的trade_date中取较大者
       */
        String temporaryTableName = "temp_tbl_hedge_id_max_trade_date_" + System.currentTimeMillis();
        final String temporaryTableSqlWithVar = """
                CREATE TEMPORARY TABLE {temporaryTableName}
                SELECT
                    hedge_id, max(t.trade_date) as max_trade_date
                FROM  `eblog`.`fortune_stock_trade` t
                WHERE hedge_id > 0
                GROUP BY t.hedge_id
                HAVING count(hedge_id) > 1
            """;
        final String temporaryTableSql = StringUtils.replace(
            temporaryTableSqlWithVar, "{temporaryTableName}", temporaryTableName
        );


        String innerSqlWithVar = """
                SELECT
                    GREATEST(m.trade_date, r.trade_date,
                        IFNULL(
                            (SELECT max(max_trade_date) FROM {temporaryTableName} t WHERE t.hedge_id in(m.id, m.hedge_id) ),
                            GREATEST(m.trade_date, r.trade_date)
                        )
                    )  as trade_date,
                    CASE
                        WHEN m.trade_type in ('RONG_BUY', 'BUY')
                            THEN -1*m.quantity*m.price-(m.yongjin_fee+m.yinhua_fee+m.guohu_fee)
                        WHEN m.trade_type in ('SELL_FOR_REPAY', 'SELL')
                            THEN 1*m.quantity*m.price-(m.yongjin_fee+m.yinhua_fee+m.guohu_fee)
                    END as earned_i
                FROM `eblog`.`fortune_stock_trade` m
                LEFT JOIN `eblog`.`fortune_stock_trade` r ON m.hedge_id=r.id
                WHERE
                    m.account_id=?
                    AND (m.hedge_id > 0 AND r.hedge_id>0)
            """;
        FortuneCommonEnums.Channel channel = ObjectUtils.defaultIfNull(req.getChannel(), FortuneCommonEnums.Channel.ALL);
        if (FortuneCommonEnums.Channel.ALL != channel) {
            innerSqlWithVar += " AND m.channel='" + channel.name() + "' ";
        }
        final String innerSql = StringUtils.replace(
            innerSqlWithVar, "{temporaryTableName}", temporaryTableName
        );
        final String finalSqlWithVar = """
                SELECT
                    SUM(a.earned_i) as earned, a.trade_date as tradeDate
                FROM ( {innerSql} ) a
                GROUP BY tradeDate
                ORDER BY tradeDate ASC
            """;
        String finalSql = StringUtils.replace(
            finalSqlWithVar, "{innerSql}", innerSql
        );
        return Pair.of(temporaryTableSql, finalSql);
    }

    public List<StockTradePO> findTrades(String code) {
        if (StringUtils.isBlank(code)) {
            return new ArrayList<>(0);
        }
        return getRepository().findAllByCode(code);
    }
/*
    public AnalyzeHedgedTradeResp analyzeHedgedOld() {
        // 数据条数达到多少才能导致本句特别耗时呢
        List<StockTradePO> hedged = this.findHedged();
        return new StockTradeEarnComputeModule().doComputeEarn(hedged, this);
    }
*/

    /*
    @Override
    public AnalyzeHedgedTradeResp analyzeHedgedCode(List<QuerySearch> querySearchList, Long currentAccountId) {
        if (CollectionUtils.isEmpty(querySearchList)) {
            return this.analyzeHedged(currentAccountId);
        }
        List<StockTradePO> hedged = getRepository().listBy(querySearchList);
        return new StockTradeEarnComputeModule().doComputeEarn(hedged, this);
    }
*/

    public List<StockTradePO> findNotHedge() {
        return getRepository().findAllByHedgeId(0L);
    }

    public List<StockTradePO> findHedged() {
        return getRepository().findAllByHedgeIdGreaterThanOrderByIdDesc(0L);
    }


    public List<NameValueVo> findCodes() {
        final List<String> distinctCodes = getRepository().findDistinctCodes();
        final List<StockPO> stockPOList = stockDAO.findAllByCodeIn(distinctCodes);
        final Map<String, String> codeAndStockMap = stockPOList.stream().collect(Collectors.toMap(StockPO::getCode, StockPO::getName));
        return distinctCodes.stream().map(e -> NameValueVo.of(codeAndStockMap.getOrDefault(e, e), e)).toList();
    }

    public int countTradedDays() {
        return getRepository().countTradedDays();
    }

    /*
    private List<Tuple2<Long, Long>> hedgeByStockTradeMonitor(List<StockTradePO> stockTrades) {
        List<StockTradeMonitorPO> monitors =
            stockTradeMonitorDAO.findAllByStatusAndTradeSysidNotNull(StockTradeMonitorEnums.Status.CLOSED);
        Map<String, StockTradeMonitorPO> sysidMap = monitors.stream()
            .collect(Collectors.toMap(StockTradeMonitorPO::getTradeSysid, e -> e));
        if (MapUtils.isEmpty(sysidMap)) {
            return List.of();
        }
        return stockTrades.stream()
            .map(e -> {
                String channelOrderId = e.getChannelOrderId();
                StockTradeMonitorPO monitor = sysidMap.get(channelOrderId);
                if (Objects.isNull(monitor)) {
                    return null;
                }

                return Tuples.of(e.getId(), monitor.getTradeId());
            })
            .filter(Objects::nonNull)
            .toList();
    }
*/

    public int countTradedDaysWithWeekends() {
        // 首个交易日，见mysql数据
        // 10	601328	RONG_BUY	20230906
        //  11	601328	SELL	    20230906
        LocalDate firstTradeDay = LocalDate.of(2023, Month.SEPTEMBER, 6);
        LocalDate today = LocalDate.now();
        // 注意加上首个交易日那天
        return (int) DateUtils.intervalDays(firstTradeDay, today) + 1;
    }

    @Transactional
    public AffectedRowsResp parseTextAndSave(SimpleTypesReq req, Long currentAccountId) {
        String text = req.getText();
        StockTradeLogParser parser = StockTradeLogParserFactory.choose(text);
        if (Objects.isNull(parser)) {
            return AffectedRowsResp.empty();
        }


        List<StockTradePO> listToSave = parser.parse(
            text,
            StockConstants.excludedCodes, StockConstants.excludedTypeCns
        );
        listToSave.forEach(e -> e.setAccountId(currentAccountId));
        List<StockTradePO> saved = this.save(listToSave);
        return AffectedRowsResp.of(saved.size());
    }

    public List<StockTradePO> findByAccountIdAndIdIn(Long accountId, List<Long> ids) {
        return getRepository().findByAccountIdAndIdIn(accountId, ids);
    }

    /*
    public void checkHedgedList() {
        List<String> codes = getRepository().findDistinctCodes();
        for (String code : codes) {
            CheckHedgedKitsResp resp = checkHedged(code);
            List<CheckHedgedKitsResp.Result> resultList = resp.getResultList();
            List<CheckHedgedKitsResp.Result> wrong = resultList.stream().filter(e -> e.getErrorMsg().contains("wrong")).toList();
            String msg = wrong.stream().map(e -> e.getErrorMsg() + ":" + e.getIds()).collect(Collectors.joining("\n"));
            if (StringUtils.isNotBlank(msg)) {
                notifyService.notifyWrongHedgedData(msg);
            }
        }
    }*/

    /*
        public AnalyzeTradeTimeDistributeChartResp analyzeTradeTimeDistributeChart(AnalyzeTradeTimeDistributeChartReq req) {
            String sqlWithVar = """
                SELECT count(id) as c, trade_time as time
                FROM `eblog`.`fortune_stock_trade`
                where account_id=? {{_CHANNEL_CONDITION_}} and code=?
                GROUP BY trade_time
                order by trade_time asc
                """;
            String replacement = "";
            FortuneCommonEnums.Channel channel = req.getChannel();
            if (FortuneCommonEnums.Channel.ALL != channel) {
                replacement = " AND channel='" + channel.name() + "' ";
            }
            sqlWithVar = StringUtils.replace(sqlWithVar, "{{_CHANNEL_CONDITION_}}", replacement);

            List<AnalyzeTradeTimeDistributeChartResp.CountByTime> list = jdbcTemplate.query(sqlWithVar,
                new BeanPropertyRowMapper<>(AnalyzeTradeTimeDistributeChartResp.CountByTime.class),
                req.getAccountId(), req.getCode());
            Map<String, Integer> map = list.stream()
                .collect(Collectors.toMap(
                    AnalyzeTradeTimeDistributeChartResp.CountByTime::getTime,
                    AnalyzeTradeTimeDistributeChartResp.CountByTime::getC)
                );
            LocalTime startTime = LocalTime.of(9, 30);
            Map<String, Integer> sortedMap = new TreeMap<>(map);
            while (startTime.isBefore(LocalTime.of(15, 0))) {
                sortedMap.putIfAbsent(DateUtils.formatTime(startTime), 0);
                startTime = startTime.plusSeconds(1);
            }
            List<AnalyzeTradeTimeDistributeChartResp.CountByTime> finalList = sortedMap.entrySet().stream().map(e -> {
                AnalyzeTradeTimeDistributeChartResp.CountByTime o = new AnalyzeTradeTimeDistributeChartResp.CountByTime();
                o.setTime(e.getKey());
                o.setC(e.getValue());
                return o;
            }).toList();

            AnalyzeTradeTimeDistributeChartResp resp = new AnalyzeTradeTimeDistributeChartResp();
            resp.setList(finalList);
            return resp;
        }
    */
    /*
    public AutoToHedgeTradeResp autoHedgePairTrade(Map<String, String> pairsUniqueId, int days) {
        AutoToHedgeTradeResp resp = new AutoToHedgeTradeResp();

        Integer preTradeDate = minusDays(LocalDate.now(), days);
        final List<StockTradePO> list = getRepository().findAllByHedgeIdAndTradeDateGreaterThan(0L, preTradeDate);

        int directHedges = 0;
        List<Tuple2<Long, Long>> idMapForHedge = hedgeByStockTradeMonitor(list);
        if (CollectionUtils.isNotEmpty(idMapForHedge)) {
            Map<Long, StockTradePO> idMap = list.stream().collect(Collectors.toMap(JpaLongIdSimpleEntity::getId, e -> e));
            for (Tuple2<Long, Long> tuple2 : idMapForHedge) {
                Long t1 = tuple2.getT1();
                Long t2 = tuple2.getT2();
                StockTradePO stockTradeT1 = idMap.get(t1);
                StockTradePO stockTradeT2 = idMap.get(t2);
                markAsHedgeEachOtherIfNeed(stockTradeT1, stockTradeT2);
            }
            List<StockTradePO> hedgedsByMonitor = list.stream().filter(e -> e.getHedgeId() > 0L).toList();
            directHedges = saveListIfNeed(hedgedsByMonitor);
        }

        if (MapUtils.isNotEmpty(pairsUniqueId)) {
            final Map<String, StockTradePO> map = list.stream().filter(e -> Objects.equals(0L, e.getHedgeId()))
                .collect(Collectors.toMap(StockTradePO::getChannelOrderId, e -> e));
            for (Map.Entry<String, String> e : pairsUniqueId.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                if (StringUtils.isNoneEmpty(key, value)) {
                    StockTradePO s1 = map.get(key);
                    StockTradePO s2 = map.get(value);
                    markAsHedgeEachOtherIfNeed(s1, s2);
                }
            }
        }
        List<StockTradePO> toSave = list.stream().filter(e -> e.getHedgeId() > 0L).toList();
        int pairHedges = saveListIfNeed(toSave);

        resp.setRecords(list.size());
        resp.setMatchedRecords(directHedges + pairHedges);
        return resp;
    }
*/
    private int saveListIfNeed(List<StockTradePO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            getRepository().persist(list);
            return list.size();
        }
        return 0;
    }

    private void markAsHedgeEachOtherIfNeed(StockTradePO s1, StockTradePO s2) {
        if (Objects.isNull(s1) || Objects.isNull(s2)) {
            return;
        }
        if (!Objects.equals(0L, s1.getHedgeId()) || !Objects.equals(0L, s2.getHedgeId())) {
            return;
        }
        TradeType tradeType1 = s1.getTradeType();
        TradeType tradeType2 = s2.getTradeType();
        if (1 != tradeType1.buyValue() + tradeType2.buyValue()) {
            return;
        }
        if (s1.getQuantity() != s2.getQuantity()) {
            return;
        }

        s1.setHedgeId(s2.getId());
        s2.setHedgeId(s1.getId());
    }

    /*
        public AnalyzeHedgedTradeResp analyzeHedged(Long currentAccountId) {
            Integer beginYyyyMMdd = DateUtils.formatAsYyyyMMdd(LocalDate.now().minusMonths(3));
            // 数据条数达到多少才能导致本句特别耗时呢
            String sql = """
                SELECT
                    id,
                    hedge_id as hedgeId,
                    code,
                    quantity,
                    price,
                    trade_type as tradeType,
                    CASE
                        WHEN m.trade_type IN ('RONG_BUY', 'BUY')
                        THEN -1 * m.quantity * m.price
                        WHEN m.trade_type IN ('SELL_FOR_REPAY', 'SELL', 'PASSIVE_SELL')
                        THEN 1 * m.quantity * m.price
                    END AS amountWithSign,
                    m.yongjin_fee + m.yinhua_fee + m.guohu_fee AS totalCommission
                FROM
                    `eblog`.`fortune_stock_trade` m
                WHERE
                    m.account_id = ? AND m.hedge_id > 0
                    and m.trade_date >= ?
                """;
            // 从当前header中获取userId
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("query");
            List<TmpVo> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TmpVo.class), 1L, beginYyyyMMdd);
            stopWatch.stop();
            stopWatch.start("computeEarn");
            AnalyzeHedgedTradeResp resp = new StockTradeEarnCompute2Module().doComputeEarn(list);
            stopWatch.stop();
            log.info("stopWatch={}", stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
            return resp;
        }
    */
    @Data
    public static class TmpVo implements Serializable {
        private Long id;
        private Long hedgeId;
        private TradeType tradeType;
        private BigDecimal amountWithSign;
        private BigDecimal totalCommission;
    }

    @Data
    private static class StatDailyDataVo implements Serializable {
        private Integer tradeDate;
        private BigDecimal earned;
    }

}
