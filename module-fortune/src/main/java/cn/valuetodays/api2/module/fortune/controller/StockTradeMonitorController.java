package cn.valuetodays.api2.module.fortune.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.persist.StockTradeMonitorPO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AutoToHedgeTradeResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsRealtimeEtfsQuoteReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsRealtimeEtfsQuoteResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.OrderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.TradeMonitorPriceResp;
import cn.valuetodays.api2.module.fortune.service.StockTradeMonitorServiceImpl;
import cn.valuetodays.api2.module.fortune.service.StockTradeServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import cn.valuetodays.quarkus.commons.base.PageQueryReqIO;
import cn.valuetodays.trade.HaitongHttpAutoTrade;
import cn.vt.R;
import cn.vt.rest.third.StockEnums;
import cn.vt.vo.NameValueVo;
import cn.vt.web.RestPageImpl;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-10
 */
@Slf4j
@Path(value = "/stockTradeMonitor")
public class StockTradeMonitorController
    extends BaseCrudController<Long, StockTradeMonitorPO, StockTradeMonitorServiceImpl> {

    @Inject
    StockRealtimeController stockRealtimeController;
    @Inject
    StockTradeServiceImpl stockTradeService;
    @Inject
    HaitongHttpAutoTrade haitongHttpAutoTrade;

    @Path("/pageQuery2")
    @POST
    public RestPageImpl<TradeMonitorPriceResp> pageQuery2(PageQueryReqIO pageQueryReqIO) {
        PageQueryReqIO pageReq = new PageQueryReqIO();
        pageReq.setPageNum(1);
        pageReq.setPageSize(2000);
        pageReq.setSearches(pageQueryReqIO.getSearches());
        pageReq.setSorts(pageQueryReqIO.getSorts());
        R<RestPageImpl<StockTradeMonitorPO>> pagedR = super.query(pageReq);
        RestPageImpl<StockTradeMonitorPO> paged = pagedR.getData();
        List<StockTradeMonitorPO> list = paged.getContent();

        EtfsRealtimeEtfsQuoteReq req = new EtfsRealtimeEtfsQuoteReq();
        req.setCodes(list.stream().map(StockTradeMonitorPO::getCode).toList());
        List<EtfsRealtimeEtfsQuoteResp> realtimeList = stockRealtimeController.realtimeEtfsQuote(req);
        Map<String, Double> codePriceMap = realtimeList.stream()
            .collect(
                Collectors.toMap(
                    e -> e.getCode().substring(StockEnums.Region.SHANGHAI.getShortCode().length()),
                    EtfsRealtimeEtfsQuoteResp::getCurrent
                )
            );

        // 准备编码与名称对应关系
        List<NameValueVo> codeInfos = stockTradeService.findCodes();
        Map<String, String> codeNameMap = codeInfos.stream()
            .collect(Collectors.toMap(NameValueVo::getValue, NameValueVo::getName));

        Map<Long, OrderInfo> codeAndOrderInfo = new HashMap<>();
        try {
            Map<String, String> param = new HashMap<>();
            param.put("strategy_name", "FIX,EMPTY");
            List<OrderInfo> orderInfos = haitongHttpAutoTrade.queryOrders(param);
            if (CollectionUtils.isNotEmpty(orderInfos)) {
                List<String> order_sysids = orderInfos.stream()
                    .filter(e -> !StringUtils.startsWith(e.getOrder_remark(), "mid"))
                    .map(OrderInfo::getOrder_sysid).toList();
                if (CollectionUtils.isNotEmpty(order_sysids)) {
                    final List<OrderInfo> orderInfoListWithRemark = haitongHttpAutoTrade.queryBySysid(order_sysids);
                    final Map<String, OrderInfo> sysidMap = orderInfoListWithRemark.stream()
                        .collect(Collectors.toMap(OrderInfo::getOrder_sysid, e -> e));
                    // merge数据 开始
                    orderInfos.forEach(e -> {
                        String orderSysid = e.getOrder_sysid();
                        OrderInfo orderInfo = sysidMap.get(orderSysid);
                        if (Objects.nonNull(orderInfo)) {
                            e.setOrder_remark(orderInfo.getOrder_remark());
                            e.setStrategy_name(orderInfo.getStrategy_name());
                        }
                    });
                    // merge数据 结束
                }
                orderInfos.forEach(e -> {
                    String orderRemark = e.getOrder_remark();
                    if (StringUtils.startsWith(orderRemark, "mid")) {
                        String idString = orderRemark.substring("mid".length());
                        Long id = Long.valueOf(idString);
                        codeAndOrderInfo.put(id, e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("error", e);
        }

        List<TradeMonitorPriceResp> monitorPriceList = list.stream()
            .map(e -> {
                TradeMonitorPriceResp monitorPriceVo = new TradeMonitorPriceResp();
                monitorPriceVo.setMonitor(e);
                monitorPriceVo.setRealtimePrice(codePriceMap.get(e.getCode()));
                monitorPriceVo.computeSuggest(e.getTradeType(), e.getPrice());
                monitorPriceVo.setXueqiuStockUrl(
                    "https://xueqiu.com/S/" +
                        Optional.ofNullable(StockEnums.Region.byCode(e.getCode()))
                            .map(StockEnums.Region::getShortCode)
                            .orElse("")
                        + e.getCode()
                );

                // 根据编码填充名称
                String code = e.getCode();
                String name = codeNameMap.get(code);
                if (Objects.nonNull(name)) {
                    monitorPriceVo.setName(name);
                }
                OrderInfo orderInfo = codeAndOrderInfo.get(e.getId());
                if (Objects.nonNull(orderInfo)) {
                    monitorPriceVo.setOrderStatusStr(orderInfo.getOrder_status_str());
                }
                return monitorPriceVo;
            })
            .toList();
        RestPageImpl<TradeMonitorPriceResp> result = new RestPageImpl<>();
        result.setContent(monitorPriceList);
        result.setPage(0);
        result.setSize(monitorPriceList.size());
        result.setTotal(monitorPriceList.size());
        result.setTotalElements(monitorPriceList.size());
        result.setTotalPages(1);
        return result;
    }

    @Path(value = "/findNormal")
    @POST
    public List<StockTradeMonitorPO> findNormal() {
        return getService().findNormal();
    }

    @Path(value = "/markAsTraded")
    public Boolean markAsTraded(SimpleTypesReq req) {
        return getService().markAsTraded(req);
    }

    @Path(value = "/feign/markAsTraded")
    public Boolean feignMarkAsTraded(SimpleTypesReq req) {
        return markAsTraded(req);
    }

    @Path(value = "/tryHedge")
    public AutoToHedgeTradeResp tryHedge() {
        return getService().tryHedge();
    }

}
