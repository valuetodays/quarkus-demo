package cn.valuetodays.trade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.reqresp.HolderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.OrderInfo;
import cn.vt.fortune.modestep.client.Constants.StrategyType;
import cn.vt.fortune.modestep.client.Constants.TradeType;
import cn.vt.fortune.modestep.client.api.HaitongApi;
import cn.vt.fortune.modestep.client.vo.AssetVo;
import cn.vt.fortune.modestep.client.vo.HolderVo;
import cn.vt.fortune.modestep.client.vo.InfoVo;
import cn.vt.fortune.modestep.client.vo.OrderVo;
import cn.vt.fortune.modestep.client.vo.RealtimePriceVo;
import cn.vt.fortune.modestep.client.vo.TradeVo;
import cn.vt.rest.third.utils.StockCodeUtils;
import cn.vt.util.ConvertUtils;
import cn.vt.util.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-09
 */
@ApplicationScoped
@Slf4j
public class HaitongHttpAutoTrade extends BaseHttpAutoTrade {

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(48, "未报");
//        ORDER_UNREPORTED = 48
        STATUS_MAP.put(49, "待报");
//        ORDER_WAIT_REPORTING = 49
        STATUS_MAP.put(50, "已报");
//        ORDER_REPORTED = 50
        STATUS_MAP.put(51, "已报待撤");
//        ORDER_REPORTED_CANCEL = 51
        STATUS_MAP.put(52, "部成待撤");
//        ORDER_PARTSUCC_CANCEL = 52
        STATUS_MAP.put(53, "部撤");
//        ORDER_PART_CANCEL = 53
        STATUS_MAP.put(54, "已撤");
//        ORDER_CANCELED = 54
        STATUS_MAP.put(55, "部成");
//        ORDER_PART_SUCC = 55
        STATUS_MAP.put(56, "已成");
//        ORDER_SUCCEEDED = 56
        STATUS_MAP.put(57, "废单");
//        ORDER_JUNK = 57
        STATUS_MAP.put(255, "未知");
//        ORDER_UNKNOWN = 255
    }

    public HaitongHttpAutoTrade() {
        prepare(QsProvider.HAITONG);
    }

    @Override
    public void preCheck() {
        AssetVo asset = HaitongApi.getAsset();
        if (Objects.isNull(asset)) {
            throw new IllegalStateException("api server is not active.");
        }
    }

    @Override
    public String doSell(String code, double price, int quantity, String extra) {
        String codeToUse = StockCodeUtils.buildForEhaifangzhou(code);
        TradeVo tradeVo = new TradeVo();
        tradeVo.setCode(codeToUse);
        tradeVo.setType(TradeType.SELL);
        tradeVo.setQuantity(quantity);
        tradeVo.setPrice(BigDecimal.valueOf(price));
        tradeVo.setExtra(extra);
        tradeVo.setStrategy_type(StrategyType.FIX);
        return doTrade0(tradeVo);
    }

    @Override
    public String doBuy(String code, double price, int quantity, String extra) {
        String codeToUse = StockCodeUtils.buildForEhaifangzhou(code);
        TradeVo tradeVo = new TradeVo();
        tradeVo.setCode(codeToUse);
        tradeVo.setType(TradeType.BUY);
        tradeVo.setQuantity(quantity);
        tradeVo.setPrice(BigDecimal.valueOf(price));
        tradeVo.setExtra(extra);
        tradeVo.setStrategy_type(StrategyType.FIX);
        return doTrade0(tradeVo);
    }

    private String doTrade0(TradeVo tradeVo) {
        try {
            Map<String, Object> respMap = HaitongApi.trade(tradeVo);
            String s = JsonUtils.toJsonString(respMap);
            if (StringUtils.contains(s, "\"code\":0")
                && StringUtils.contains(s, "\"order_flag\":true")) {
                // 成功
                return null;
            }
            return s;
        } catch (Exception e) {
            String message = e.getMessage();
            if (StringUtils.contains(message, "Faithfully yours, frp.")
                && StringUtils.containsAny(message, "The page you requested was not found.")) {
                return "请求无法连接，请检查frp中服务端和客户端分别对应的端口是否启动";
            }
            log.error("error when doTrade0", e);
            return e.getMessage();
        }
    }

    public List<OrderVo> getOrders(Map<String, String> req) {
        return HaitongApi.getOrders(req);
    }

    @Override
    protected AccountInfo getAccountInfo() {
        AssetVo asset = HaitongApi.getAsset();
        if (Objects.isNull(asset)) {
            return null;
        }
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAvailableUseMoney(asset.getCash());
        accountInfo.setAvailableWithdrawMoney(asset.getCash());
        accountInfo.setFreezeMoney(asset.getFrozen_cash());
        accountInfo.setStockMarketMoney(asset.getMarket_value());
        accountInfo.setTotalMoney(asset.getTotal_asset());
        return accountInfo;
    }

    @Override
    public List<HolderInfo> parseHolderInfo(String holderInfoStr) {
        List<HolderVo> holders = HaitongApi.getHolders();
        return holders.stream()
            .map(e -> {
                String stockCode = e.getStock_code();
                HolderInfo hi = new HolderInfo();
                hi.setCode(stockCode);
                hi.setName("-");
                hi.setStockQuantity(e.getVolume());
                hi.setStockAvailableQuantity(e.getCan_use_volume());
                BigDecimal openPrice = e.getOpen_price();
                if (Objects.isNull(openPrice) || openPrice.compareTo(BigDecimal.ZERO) < 0) {
                    openPrice = BigDecimal.ZERO;
                }
                hi.setCostPrice(openPrice);
                hi.setMarketPrice(BigDecimal.ZERO);
                hi.setShareAccount("海通");
                return hi;
            })
            .toList();
    }

    @Override
    public List<OrderInfo> queryOrders(Map<String, String> param) {
        /*
    json_dict = get_biz_json()
    code = json_dict.get('code', '')
    order_sysid = json_dict.get('order_sysid', '')
    # 0,24,2
    order_status = json_dict.get('order_status', '')
    # str1,str2,str3
    strategy_name = json_dict.get('strategy_name', '')
    cancelable_only = json_dict.get('cancelable_only', '1')
         */
        List<OrderVo> orders = HaitongApi.getOrders(param);
        return toOrderInfoList(orders);
    }

    private List<OrderInfo> toOrderInfoList(List<OrderVo> orders) {
        return orders.stream()
            .map(e -> {
                OrderInfo oi = ConvertUtils.convertObj(e, OrderInfo.class);
                oi.setOrder_status_str(STATUS_MAP.getOrDefault(oi.getOrder_status(), "-"));
                return oi;
            })
            .toList();
    }

    public List<InfoVo> batchGetInfo(List<String> codes) {
        return HaitongApi.batchGetInfo(codes);
    }

    public List<OrderInfo> queryBySysid(List<String> sysids) {
        List<OrderVo> orders = HaitongApi.queryBySysid(sysids);
        return toOrderInfoList(orders);
    }

    public RealtimePriceVo realtimePrice(String code) {
        List<RealtimePriceVo> realtimePriceInfos = realtimePrice(List.of(code));
        if (CollectionUtils.isEmpty(realtimePriceInfos)) {
            return null;
        }
        return realtimePriceInfos.get(0);
    }

    public List<RealtimePriceVo> realtimePrice(List<String> codes) {
        List<String> codesWithMarket = codes.stream()
            .map(StockCodeUtils::buildForEhaifangzhou)
            .distinct()
            .toList();
        return HaitongApi.realtimePrice(codesWithMarket);
    }

}
