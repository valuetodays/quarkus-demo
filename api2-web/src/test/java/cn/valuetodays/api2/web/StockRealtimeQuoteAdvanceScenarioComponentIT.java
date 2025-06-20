package cn.valuetodays.api2.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.valuetodays.api2.module.fortune.reqresp.EtfsCompareResp;
import cn.valuetodays.api2.module.fortune.service.module.StockRealtimeQuoteAdvanceScenarioComponent;
import cn.vt.test.TestBase;
import org.junit.jupiter.api.Test;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-05-23 16:14
 */
class StockRealtimeQuoteAdvanceScenarioComponentIT extends TestBase {

    @Test
    public void compareEtfs() {
        // 每次交易的最小金额
        final int minMoneyPerTrade = 2000 * 5;

        StockRealtimeQuoteAdvanceScenarioComponent srqasc = new StockRealtimeQuoteAdvanceScenarioComponent();
        List<List<String>> etfLists = Arrays.asList(
            Arrays.asList("SH515710", "SH515170"),
            Arrays.asList("SH510300", "SH510310"),
            Arrays.asList("SZ159847", "SH512170"),
            Arrays.asList("SH510580", "SH510500"),
            Arrays.asList("SH513330", "SZ159605", "SZ159607")
        );
        for (List<String> etfList : etfLists) {
            EtfsCompareResp resp = srqasc.processCompare("", 1, etfList, minMoneyPerTrade);
            List<EtfsCompareResp.Result> infos = resp.getInfos();
            List<EtfsCompareResp.Suggest> suggests = resp.getSuggests();
            List<String> finalList = new ArrayList<>(suggests.size());
            finalList.addAll(
                infos.stream()
                    .map(e -> e.getCode() + ": 开 " + e.getOpen() + " 当 " + e.getCurrent() + " 均 " + e.getAvgPrice())
                    .toList()
            );
            for (EtfsCompareResp.Suggest suggest : suggests) {
                String codeToSell = suggest.getCodeToSell();
                double priceToSell = suggest.getPriceToSell();
                int quantityToSell = suggest.getQuantityToSell();
                String codeToBuy = suggest.getCodeToBuy();
                double priceToBuy = suggest.getPriceToBuy();
                int quantityToBuy = suggest.getQuantityToBuy();
                double currentPriceOfBuy = suggest.getCurrentPriceOfBuy();

                String tip = "交易建议：\nsell " + codeToSell + " @ " + priceToSell + " x " + quantityToSell
                    + ", and buy " + codeToBuy + " @ " + priceToBuy + " x " + quantityToBuy
                    + "(now: " + currentPriceOfBuy + ", net get ¥" + suggest.getNetSavedMoney()
                    + ", fee: " + suggest.getTotalFee() + ")";
                finalList.add(tip);
            }
            getLogger().info("all: \n{}\n", String.join("\n", finalList));
        }
    } // end of compareEtfs()

    @Test
    public void computeBuySellSuggestion() {
        final int minMoneyPerTrade = 2000 * 5;
        StockRealtimeQuoteAdvanceScenarioComponent c = new StockRealtimeQuoteAdvanceScenarioComponent();
        EtfsCompareResp.Suggest suggest = c.computeBuySellSuggestion(minMoneyPerTrade,
            "C1", 1.0, 0.9,
            "C2", 1.0, 1.7
        );
        getLogger().info("\n{}", suggest);
    }
}
