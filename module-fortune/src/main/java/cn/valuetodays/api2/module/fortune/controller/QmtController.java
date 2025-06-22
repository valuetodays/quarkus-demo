package cn.valuetodays.api2.module.fortune.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.valuetodays.api2.module.fortune.client.QmtConstants;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradeMonitorPO;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradePO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AutoToHedgeTradeResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.DealedOrderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.HolderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.OrderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.QmtAnalysisOrdersResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.SaveAllHoldersReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockTradeOrderVo;
import cn.valuetodays.api2.module.fortune.client.reqresp.TradeByMonitorIdReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.TradeByMonitorIdResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.UpdateStockTradeMonitorByQmtOrderWithFixAndSucResp;
import cn.valuetodays.api2.module.fortune.client.util.PriceUtilsEx;
import cn.valuetodays.api2.module.fortune.client.util.QmtUtils;
import cn.valuetodays.api2.module.fortune.component.QmtComponent;
import cn.valuetodays.api2.module.fortune.service.StockHoldersServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockTradeMonitorServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockTradeServiceImpl;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.quarkus.commons.base.BaseAuthorizationController;
import cn.valuetodays.trade.HaitongHttpAutoTrade;
import cn.vt.fortune.modestep.client.api.HaitongApi;
import cn.vt.fortune.modestep.client.vo.FullTickVo;
import cn.vt.fortune.modestep.client.vo.HolderVo;
import cn.vt.fortune.modestep.client.vo.InfoVo;
import cn.vt.fortune.modestep.client.vo.OrderVo;
import cn.vt.fortune.modestep.client.vo.RealtimePriceVo;
import cn.vt.moduled.fortune.StockConstants;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.rest.third.utils.ITradeFeeConf;
import cn.vt.rest.third.utils.StockCodeUtils;
import cn.vt.rest.third.utils.StockTradeFeeUtils;
import cn.vt.rest.third.utils.StockUtils;
import cn.vt.rest.third.utils.TradeFeeConfConstants;
import cn.vt.util.ConvertUtils;
import cn.vt.util.DateUtils;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-01
 */
@Path(value = "/qmt")
public class QmtController extends BaseAuthorizationController {
    private static final ConcurrentHashMap<String, RealtimePriceVo> UP_STOP_DOWN_STOP_INFO = new ConcurrentHashMap<>();
    @Inject
    StockTradeMonitorServiceImpl stockTradeMonitorService;
    @Inject
    HaitongHttpAutoTrade haitongHttpAutoTrade;
    @Inject
    StockHoldersServiceImpl stockHoldersService;
    @Inject
    StockTradeServiceImpl stockTradeService;
    @Inject
    QmtComponent qmtComponent;

    static BigDecimal makeNotOutOfRange(BigDecimal priceToTrade, BigDecimal realtimePrice, RealtimePriceVo info) {
        if (Objects.isNull(info)) {
            return priceToTrade;
        }
        BigDecimal upStopPrice = info.getUpStopPrice();
        BigDecimal downStopPrice = info.getDownStopPrice();
        if (priceToTrade.compareTo(downStopPrice) < 0 || priceToTrade.compareTo(upStopPrice) > 0) {
            return realtimePrice;
        }
        return priceToTrade;
    }

    static BigDecimal choosePriceToSell(BigDecimal realtimePrice, BigDecimal price) {
        // use lower price to sell, this will make the order be traded easily
        if (realtimePrice.compareTo(price) > 0) {
            return price;
        } else {
            return price.max(realtimePrice);
        }
    }

    static BigDecimal choosePriceToBuy(BigDecimal realtimePrice, BigDecimal price) {
        // use higher price to buy, this will make the order be traded easily
        if (realtimePrice.compareTo(price) < 0) {
            return price;
        }
        return price.min(realtimePrice);
    }

    public static StockTradePO fromOrderVo(OrderVo e) {
        String code = StockCodeUtils.parseFromEhaifangzhou(e.getStock_code());
        if (StockConstants.excludedCodes.contains(code)) {
            return null;
        }

        StockTradePO stockTradeOrderVo = new StockTradePO();
        stockTradeOrderVo.setId(Long.valueOf(e.getOrder_sysid()));
        Integer orderType = e.getOrder_type();
        if (orderType.equals(QmtConstants.STOCK_SELL)) {
            stockTradeOrderVo.setTradeType(FortuneCommonEnums.TradeType.SELL);
        } else if (orderType.equals(QmtConstants.STOCK_BUY)) {
            stockTradeOrderVo.setTradeType(FortuneCommonEnums.TradeType.BUY);
        } else {
            return null;
        }
        stockTradeOrderVo.setCode(e.getStock_code());
        stockTradeOrderVo.setQuantity(e.getTraded_volume());
        stockTradeOrderVo.setPrice(e.getTraded_price());
        stockTradeOrderVo.setHedgeId(0L);
        stockTradeOrderVo.setRemark(e.getStrategy_name()); // put here to be used
        String remark = stockTradeOrderVo.getRemark();
        // 结束订单中有原始订单的id
        if (StringUtils.containsAny(remark,
            QmtConstants.StrategyType.PAIR_AE.name(),
            QmtConstants.StrategyType.PAIR_ME.name(),
            QmtConstants.StrategyType.OFFSET_AE.name())) {
            try {
                String orderRemark = e.getOrder_remark();
                stockTradeOrderVo.setHedgeId(Long.valueOf(orderRemark));
            } catch (Exception ignored) {
            }
        }
        return stockTradeOrderVo;
    }

    @Path(value = "/tryTrade")
    @POST
    public TradeByMonitorIdResp tryTrade(@Valid TradeByMonitorIdReq req) {
        final Long monitorId = req.getMonitorId();
        final BigDecimal realtimePrice = req.getRealtimePrice();

        if (monitorId <= 0) {
            return TradeByMonitorIdResp.empty("monitorId <= 0");
        }
        final StockTradeMonitorPO po = stockTradeMonitorService.findById(monitorId);
        if (Objects.isNull(po)) {
            return TradeByMonitorIdResp.empty("no record with monitorId=" + monitorId);
        }
        FortuneCommonEnums.TradeType tradeType = po.getTradeType();
        String code = po.getCode();
        int quantity = po.getQuantity();
        BigDecimal price = po.getPrice();
        String errorMsg;

        RealtimePriceVo realtimePriceInfo = null;
        if (StockUtils.isInTradeTime(LocalTime.now())) {
            final String key = code + "-" + DateUtils.formatDateToday();
            realtimePriceInfo = UP_STOP_DOWN_STOP_INFO.get(key);
            if (Objects.isNull(realtimePrice)) {
                realtimePriceInfo = haitongHttpAutoTrade.realtimePrice(code);
                UP_STOP_DOWN_STOP_INFO.put(key, realtimePriceInfo);
            }
        }

        if (FortuneCommonEnums.TradeType.BUY == tradeType) {
            BigDecimal priceToBuy = choosePriceToBuy(realtimePrice, price);
            priceToBuy = makeNotOutOfRange(priceToBuy, realtimePrice, realtimePriceInfo);
            errorMsg = haitongHttpAutoTrade.doBuy(code, priceToBuy.doubleValue(), quantity, "mid" + po.getId());
        } else {
            BigDecimal priceToSell = choosePriceToSell(realtimePrice, price);
            priceToSell = makeNotOutOfRange(priceToSell, realtimePrice, realtimePriceInfo);
            errorMsg = haitongHttpAutoTrade.doSell(code, priceToSell.doubleValue(), quantity, "mid" + po.getId());
        }
        if (Objects.nonNull(errorMsg)) {
            return TradeByMonitorIdResp.empty(errorMsg);
        } else {
            return TradeByMonitorIdResp.of(1);
        }
    }

    /**
     * 通过qmt获取持仓并保存
     */
    @Path("/saveHolders")
    @POST
    public AffectedRowsResp saveHolders() {
        List<HolderInfo> holderInfos = haitongHttpAutoTrade.parseHolderInfo(null);
        if (CollectionUtils.isEmpty(holderInfos)) {
            return AffectedRowsResp.empty();
        }
        List<String> codes = holderInfos.stream().map(HolderInfo::getCode).toList();
        List<InfoVo> infos = haitongHttpAutoTrade.batchGetInfo(codes);
        Map<String, InfoVo> infoMap = infos.stream()
            .collect(Collectors.toMap(InfoVo::getInstrumentID, e -> e));
        holderInfos.forEach(e -> {
            String codeWithoutMarket = e.getCode().substring(0, "513360".length());
            e.setCode(codeWithoutMarket);
            InfoVo info = infoMap.get(codeWithoutMarket);
            if (Objects.nonNull(info)) {
                e.setName(info.getInstrumentName());
                e.setMarketPrice(info.getLastClose());
            }
        });
        List<HolderInfo> holderInfosToUse = holderInfos.stream()
            .filter(e -> e.getStockQuantity() > 0)
            .filter(e -> !StockConstants.excludedCodes.contains(e.getCode()))
            .toList();
        SaveAllHoldersReq req = new SaveAllHoldersReq();
        req.setHolderInfos(holderInfosToUse);
        req.setDate(DateUtils.formatAsYyyyMMdd(LocalDate.now()));
        req.setChannel(FortuneCommonEnums.Channel.HAITONG);
        int n = stockHoldersService.saveAllHolders(req, getCurrentAccountId());
        return AffectedRowsResp.of(n);
    }

    /**
     * 通过qmt获取成交并保存
     */
    @Path("/saveStockTradeLog")
    @POST
    public AffectedRowsResp saveStockTradeLog() {
        Map<String, String> req = Map.of(
            // 部分成交的委托，也视为成交的委托
            // 部分成交的委托被撤销了，会变成部销，也视为成交的委托
            "order_status", StringUtils.joinWith(",",
                QmtConstants.ORDER_PART_SUCC, QmtConstants.ORDER_SUCCEEDED, QmtConstants.ORDER_PART_CANCEL
            ),
            "cancelable_only", "0"
        );
        List<OrderInfo> orderInfos = haitongHttpAutoTrade.queryOrders(req);
        if (CollectionUtils.isEmpty(orderInfos)) {
            return AffectedRowsResp.empty();
        }
        List<DealedOrderInfo> dealedOrderInfos = computeFee(orderInfos);
        AffectedRowsResp resp = stockTradeService.saveByOrderInfos(dealedOrderInfos,
            FortuneCommonEnums.Channel.HAITONG, getCurrentAccountId());
        Map<String, String> pairsUniqueId = dealedOrderInfos.stream()
            .filter(e -> QmtConstants.END_TRADES.contains(e.getStrategy_name()))
            .collect(Collectors.toMap(
                QmtUtils::formatOrderUniqueId,
                e -> {
                    String orderRemark = e.getOrder_remark();
                    return QmtUtils.formatOrderDateAsYyyyMMdd(e.getOrder_time()) + orderRemark;
                }
            ));

        stockTradeService.autoHedgePairTrade(pairsUniqueId, 1);
        return resp;
    }

    public String analysisOrdersAsString() {
        QmtAnalysisOrdersResp resp = this.analysisOrders();
        if (Objects.nonNull(resp)) {
            return resp.buildResultMsg();
        } else {
            return "ERROR: null QmtAnalysisOrdersResp";
        }
    }

    /**
     * 获取委托
     */
//    @Path({"/feign/getOrders.do", "/feign/getSuccessOrders.do"})
    @Path("/feign/getSuccessOrders")
    @POST
    public List<OrderVo> getSuccessOrders() {
        return this.getOrders(List.of(QmtConstants.ORDER_SUCCEEDED), "0");
    }

    private List<OrderVo> getOrders(List<Integer> orderStatusList, String cancelable_only) {
        Map<String, String> req = Map.of(
            "order_status", StringUtils.join(orderStatusList, ","),
            "cancelable_only", cancelable_only
        );
        return haitongHttpAutoTrade.getOrders(req);
    }

    /**
     * 分析委托
     */
    @Path("/feign/analysisOrders")
    @POST
    public QmtAnalysisOrdersResp analysisOrders() {
        try {
            List<OrderVo> succeedList = this.getSuccessOrders();
            if (CollectionUtils.isEmpty(succeedList)) {
                return QmtAnalysisOrdersResp.ofError("no succeed orders");
            }
            return analysisOrders(succeedList);
        } catch (Exception e) {
            QmtAnalysisOrdersResp resp = new QmtAnalysisOrdersResp();
            resp.setError("error: " + e.getMessage());
            return resp;
        }
    }

    QmtAnalysisOrdersResp analysisOrders(List<OrderVo> succeedList) {
        QmtAnalysisOrdersResp resp = new QmtAnalysisOrdersResp();
        resp.setSucceedOrders(succeedList.size());
        final List<StockTradePO> orders = succeedList.stream()
            .map(QmtController::fromOrderVo)
            .filter(Objects::nonNull)
            // strategy named FIX will not participate hedge
            .filter(e -> !StringUtils.containsIgnoreCase(e.getRemark(), "FIX"))
            .toList();
        final Map<Long, StockTradeOrderVo> idAndOrderMap = orders.stream()
            .map(e -> ConvertUtils.convertObj(e, StockTradeOrderVo.class))
            .collect(Collectors.toMap(StockTradeOrderVo::getId, e -> e));
        final List<Long> hedgedIdsPre = orders.stream()
            .filter(e -> e.getHedgeId() > 0L)
            .flatMap(e -> Stream.of(e.getId(), e.getHedgeId()))
            .toList();
        List<StockTradePO> notHedgedOrders = orders.stream()
            .filter(e -> !hedgedIdsPre.contains(e.getId()))
            .toList();
        AutoToHedgeTradeResp autoHedgeOrdersResp = StockTradeServiceImpl.autoHedgeOrders(notHedgedOrders);

        // ///////////////////////////
        Map<Long, Long> idMap = autoHedgeOrdersResp.getIdMap();
        final List<Long> hedgedIdsPost = idMap.entrySet().stream()
            .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
            .distinct()
            .toList();
        final List<Long> hedgedIdsForDistinct = new ArrayList<>(hedgedIdsPre.size() + hedgedIdsPost.size());
        hedgedIdsForDistinct.addAll(hedgedIdsPre);
        hedgedIdsForDistinct.addAll(hedgedIdsPost);
        final List<Long> hedgedIds = hedgedIdsForDistinct.stream().distinct().toList();
        final List<StockTradeOrderVo> hedgedOrdersByPair = hedgedIds.stream()
            .map(e -> {
                StockTradeOrderVo order = idAndOrderMap.get(e);
                if (Objects.nonNull(order)) {
                    order.compute();
                }
                return order;
            })
            .filter(Objects::nonNull)
            .toList();
        resp.setMatchedOrders(hedgedOrdersByPair.size());
        BigDecimal earnedTotal = hedgedOrdersByPair.stream()
            .map(e -> e.getAmountWithSign().subtract(e.getFee()))
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        resp.setAmount_pair_total_decimal(earnedTotal);
        resp.setCount_orders(hedgedOrdersByPair.size());

        List<StockTradeOrderVo> hedgedOrdersByAutoPair = hedgedOrdersByPair.stream()
            .filter(e -> QmtConstants.AUTO_PAIR_NAMES.contains(e.getRemark()))
            .toList();
        BigDecimal earnedTotalByAutoPair = hedgedOrdersByAutoPair.stream()
            .map(e -> e.getAmountWithSign().subtract(e.getFee()))
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        resp.setAmount_pair_auto_decimal(earnedTotalByAutoPair);
        resp.setCount_orders_auto(hedgedOrdersByAutoPair.size());

        // 未成交的买单会占用资金，需要排除已对冲，只包含发起订单
        final List<StockTradeOrderVo> buyList = orders.stream()
            .map(e -> idAndOrderMap.get(e.getId()))
            .filter(e -> e.getTradeType() == FortuneCommonEnums.TradeType.BUY)
            .filter(e -> !hedgedIds.contains(e.getId()))
            .filter(e -> QmtConstants.END_STRATEGY_LIST.contains(e.getRemark()))
            .toList();
        BigDecimal money_occupy_total = BigDecimal.ZERO;
        int order_occupy_total = 0;
        BigDecimal money_occupy_auto_pair = BigDecimal.ZERO;
        int order_occupy_auto_pair = 0;
        for (StockTradeOrderVo buy : buyList) {
            buy.compute();
            String strategyName = buy.getRemark();
            if (StringUtils.equals(strategyName, QmtConstants.StrategyType.PAIR_AB.name())) {
                money_occupy_auto_pair = money_occupy_auto_pair.add(buy.getAmount());
                order_occupy_auto_pair++;
            }
            money_occupy_total = money_occupy_total.add(buy.getAmount());
            order_occupy_total++;
        }
        resp.setMoney_occupy_total(money_occupy_total);
        resp.setOrder_occupy_total(order_occupy_total);
        resp.setMoney_occupy_auto_pair(money_occupy_auto_pair);
        resp.setOrder_occupy_auto_pair(order_occupy_auto_pair);
        // ///////////////////////////

        return resp;
    }

    private List<DealedOrderInfo> computeFee(List<OrderInfo> succeedList) {
        return succeedList.stream()
            .map(e -> {
                String stockCode = e.getStock_code();
                String code = StockCodeUtils.parseFromEhaifangzhou(stockCode);
                if (StockConstants.excludedCodes.contains(code)) {
                    return null;
                }
                ITradeFeeConf tradeFeeConf = TradeFeeConfConstants.getTradeFeeConfByCode(code);
                DealedOrderInfo d = ConvertUtils.convertObj(e, DealedOrderInfo.class);
                d.setTraded_price(PriceUtilsEx.fixPrice(d.getTraded_price()));
                BigDecimal yongJinFee = StockTradeFeeUtils.computeYongJinFee(
                    d.getTraded_price(), BigDecimal.valueOf(d.getTraded_volume()),
                    tradeFeeConf);
                d.setYjFee(yongJinFee);
                ITradeFeeConf.Type type = tradeFeeConf.type();
                if (ITradeFeeConf.Type.STOCK == type) {
                    // 当是股票时，计算过户费，印花税，     过户费在卖出时才收，
                    // 深市没有过户费?
                    FortuneCommonEnums.TradeType tradeType = QmtConstants.TRADE_TYPE_MAP.get(e.getOrder_type());
                    BigDecimal yinhuaFee = StockTradeFeeUtils.computeYinhuaFee(
                        d.getTraded_price(), BigDecimal.valueOf(d.getTraded_volume()),
                        tradeType,
                        tradeFeeConf
                    );
                    d.setYhFee(yinhuaFee);
                    BigDecimal guohuFee = StockTradeFeeUtils.computeGuohuFee(
                        d.getTraded_price(), BigDecimal.valueOf(d.getTraded_volume()),
                        code,
                        tradeFeeConf
                    );
                    d.setGhFee(guohuFee);
                }
                return d;
            })
            .filter(Objects::nonNull)
            .toList();
    }


    public QmtAnalysisHoldersResp analysisHolders() {
        List<HolderVo> holders = HaitongApi.getHolders();
        List<String> excludedCodes = List.of("SHRQ88.SH");
        holders = holders.stream()
            .filter(e -> !excludedCodes.contains(e.getStock_code())) // 排除掉一些
            .filter(e -> e.getVolume() > 0)
            .toList();
        List<String> codes = holders.stream().map(HolderVo::getStock_code).toList();
        List<FullTickVo> infos = HaitongApi.getFullTick(codes);
        Map<String, FullTickVo> infoMap = infos.stream()
            .collect(Collectors.toMap(FullTickVo::getCode, e -> e));
        holders.forEach(e -> {
            String stockCodeFull = e.getStock_code();
            FullTickVo fullTickVo = infoMap.get(stockCodeFull);
            if (Objects.nonNull(fullTickVo)) {
                e.setMarketPrice(fullTickVo.getLastClose());
            }
        });
        final BigDecimal sum = holders.stream()
            .map(HolderVo::computeMarketValue)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        QmtAnalysisHoldersResp resp = new QmtAnalysisHoldersResp();
        resp.setSum(sum);

        holders.forEach(e -> e.computePercentage(sum));
        final List<HolderVo> sortedHolders = holders.stream()
            .sorted(Comparator.comparing(HolderVo::getPercentage).reversed())
            .toList();
        resp.setHolders(sortedHolders);
        return resp;

    }

    public String analysisHoldersAsString() {
        QmtAnalysisHoldersResp resp = this.analysisHolders();
        if (Objects.nonNull(resp)) {
            return resp.buildResultMsg();
        } else {
            return "ERROR: null QmtAnalysisHoldersResp";
        }
    }

    @Path(value = "/updateStockTradeMonitorByQmtOrderWithFixAndSuc")
    @POST
    public UpdateStockTradeMonitorByQmtOrderWithFixAndSucResp updateStockTradeMonitorByQmtOrderWithFixAndSuc(
        SimpleTypesReq req
    ) {
        Map<String, String> m = Map.of(
            "order_status", StringUtils.joinWith(",", QmtConstants.ORDER_SUCCEEDED),
            "cancelable_only", "0",
            "strategy_name", "FIX"
        );
        List<OrderInfo> orderInfos = haitongHttpAutoTrade.queryOrders(m);
        List<Boolean> flags = new ArrayList<>(orderInfos.size());
        for (OrderInfo orderInfo : orderInfos) {
            boolean flag = qmtComponent.markAsTraded(orderInfo);
            flags.add(flag);
        }
        var r = new UpdateStockTradeMonitorByQmtOrderWithFixAndSucResp();
        r.setTotalOrders(flags.size());
        r.setUpdatedStockTradeMonitor((int) flags.stream().filter(Boolean.TRUE::equals).count());
        return r;
    }

}
