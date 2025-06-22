package cn.valuetodays.trade.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.reqresp.AllEtfsCompareReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsCompareReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsCompareResp;
import cn.vt.R;
import cn.vt.util.HttpClient4Utils;
import cn.vt.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-26 13:31
 */
public class StrategySuggestApi {
    private static String strategyApiUrl = "";
    private static String strategyT0ExchangeApiUrl = "";

    public static void initStrategyApiUrl(String url) {
        StrategySuggestApi.strategyApiUrl = url;
    }

    public static void initStrategyT0ExchangeApiUrl(String url) {
        StrategySuggestApi.strategyT0ExchangeApiUrl = url;
    }

    public static List<EtfsCompareResp.Suggest> getSuggestsInEtfGroup(List<String> codes,
                                                                      int minMoneyPerTrade, double minNetProfit) {
        EtfsCompareReq etfsCompareReq = new EtfsCompareReq();
        etfsCompareReq.setGroup("-");
        etfsCompareReq.setTn(0);
        etfsCompareReq.setEtfs(codes);
        etfsCompareReq.setMinMoneyPerTrade(minMoneyPerTrade);

        String apiRespString = HttpClient4Utils.doPostJson(strategyT0ExchangeApiUrl, etfsCompareReq, null);
        R<EtfsCompareResp> r = JsonUtils.fromJson(apiRespString, new TypeReference<>() {
        });
        List<EtfsCompareResp.Suggest> suggestsToUse = new ArrayList<>();
        EtfsCompareResp etfsCompareResp = r.checkAndGetData();
        fillRemark(minNetProfit, etfsCompareResp, suggestsToUse);
        return suggestsToUse;
    }

    public static List<EtfsCompareResp.Suggest> getSuggests(int minMoneyPerTrade, double minNetProfit) {
        AllEtfsCompareReq allEtfsCompareReq = new AllEtfsCompareReq();
        List<EtfsCompareReq> reqList = new ArrayList<>();
        allEtfsCompareReq.setReqs(reqList);
        allEtfsCompareReq.setTn(0);
        allEtfsCompareReq.setMinMoneyPerTrade(minMoneyPerTrade);

        String apiRespString = HttpClient4Utils.doPostJson(strategyApiUrl, allEtfsCompareReq, null);
        R<List<EtfsCompareResp>> r = JsonUtils.fromJson(apiRespString, new TypeReference<R<List<EtfsCompareResp>>>() {
        });
        List<EtfsCompareResp.Suggest> suggestsToUse = new ArrayList<>();

        List<EtfsCompareResp> etfsCompareResps = r.checkAndGetData();
        for (EtfsCompareResp etfsCompareResp : etfsCompareResps) {
            fillRemark(minNetProfit, etfsCompareResp, suggestsToUse);
        }

        return suggestsToUse;
    }

    private static void fillRemark(double minNetProfit,
                                   EtfsCompareResp etfsCompareResp,
                                   List<EtfsCompareResp.Suggest> suggestsToUse) {
        List<EtfsCompareResp.Suggest> suggests = etfsCompareResp.getSuggests();
        if (CollectionUtils.isEmpty(suggests)) {
            return;
        }

        Map<String, List<EtfsCompareResp.Suggest>> tmpSuggestMap = new HashMap<>();
        for (EtfsCompareResp.Suggest suggest : suggests) {
            String codeToSell = suggest.getCodeToSell();
            double priceToSell = suggest.getPriceToSell();
            int quantityToSell = suggest.getQuantityToSell();
            String codeToBuy = suggest.getCodeToBuy();
            double priceToBuy = suggest.getPriceToBuy();
            int quantityToBuy = suggest.getQuantityToBuy();
            double extraSavedMoney = suggest.getExtraSavedMoney();
            double netSavedMoney = suggest.getNetSavedMoney();
            double totalFee = suggest.getTotalFee();
            String tip = "sell " + codeToSell + " @ " + priceToSell + " x " + quantityToSell
                + ", and buy " + codeToBuy + " @ " + priceToBuy + " x " + quantityToBuy
                + "(net get ¥" + suggest.getNetSavedMoney()
                + ", fee: " + suggest.getTotalFee() + ")";
            if (BigDecimal.valueOf(netSavedMoney).compareTo(BigDecimal.valueOf(minNetProfit)) >= 0) {
                tip = "交易建议【可行, >" + minNetProfit + "】：" + tip;
            } else {
                tip = "交易建议：" + tip;
            }
            suggest.setRemark(tip);
            List<EtfsCompareResp.Suggest> tmpList = tmpSuggestMap.get(codeToSell);
            if (Objects.isNull(tmpList)) {
                tmpList = new ArrayList<>();
                tmpSuggestMap.put(codeToSell, tmpList);
            }
            tmpSuggestMap.get(codeToSell).add(suggest);
        }
        for (Map.Entry<String, List<EtfsCompareResp.Suggest>> stringListEntry : tmpSuggestMap.entrySet()) {
            List<EtfsCompareResp.Suggest> values = stringListEntry.getValue();
            EtfsCompareResp.Suggest top1 = values.stream()
                .max(Comparator.comparingDouble(EtfsCompareResp.Suggest::getNetSavedMoney))
                .orElse(null);
            CollectionUtils.addIgnoreNull(suggestsToUse, top1);
        }
    }
}
