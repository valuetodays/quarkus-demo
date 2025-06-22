package cn.valuetodays.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.reqresp.EtfsCompareResp;
import cn.valuetodays.api2.module.fortune.client.reqresp.HolderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.OrderInfo;
import cn.valuetodays.trade.api.StrategySuggestApi;
import cn.vt.rest.third.utils.StockUtils;
import cn.vt.util.SleepUtils;
import cn.vt.util.YamlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-04-09
 */
@Slf4j
public abstract class BaseHttpAutoTrade {
    protected final Map<String, HolderInfo> holderInfoMap = new HashMap<>();
    private final AccountInfo accountInfo = new AccountInfo();
    protected TradeConfProperties confProperties;

    public BaseHttpAutoTrade() {
    }

    public abstract void preCheck();

    /**
     * @return 返回异常信息，null为无异常
     */
    public abstract String doSell(String code, double price, int quantity, String extra);

    /**
     * @return 返回异常信息，null为无异常
     */
    public abstract String doBuy(String code, double price, int quantity, String extra);

    protected abstract AccountInfo getAccountInfo();

    public abstract List<HolderInfo> parseHolderInfo(String holderInfoStr);

    public abstract List<OrderInfo> queryOrders(Map<String, String> param);

    final void fillHolderInfo(String holderInfoStr) {
        List<HolderInfo> parsedHolderInfos = this.parseHolderInfo(holderInfoStr);
        if (CollectionUtils.isNotEmpty(parsedHolderInfos)) {
            synchronized (holderInfoMap) {
                holderInfoMap.clear();
                holderInfoMap.putAll(
                    parsedHolderInfos.stream().collect(Collectors.toMap(e -> e.getCode().substring(0, 6), e -> e))
                );
            }
            log.info("holderInfoMap={}", holderInfoMap);
        }
    }

    private void initInternal() {
        this.confProperties = YamlUtils.classpathFileToObject(
            "trade.conf.yml", "trade", TradeConfProperties.class
        );
        String dryRun = System.getProperty("trade.flags.dryRun");
        if (StringUtils.isNotBlank(dryRun)) {
            confProperties.getFlags().setDryRun(Boolean.parseBoolean(dryRun));
        }
    }

    public final void prepare(QsProvider qsProvider) {
        prepareAutoTrade();
        confProperties.setQsProvider(qsProvider);
    }

    private void prepareAutoTrade() {
        initInternal();
    }

    public void doAutoTrade() {
        StrategySuggestApi.initStrategyApiUrl(confProperties.getStrategyApiUrl());
        StrategySuggestApi.initStrategyT0ExchangeApiUrl(confProperties.getStrategyT0ExchangeApiUrl());
        processLoop();
        log.info("done");
    }

    private void sleep(long millis) {
        SleepUtils.sleep(millis);
    }

    private void processLoop() {
        if (confProperties.getFlags().isDryRun()) {
            LocalDateTime startup = LocalDateTime.now();
            LocalDateTime endLdt = startup.plusHours(1);
            while (LocalDateTime.now().isBefore(endLdt)) {
                fillAccountInfo();
                sleep(1000);
            }
        } else {
            fillAccountInfo();
            while (true) {
                LocalTime startup = LocalTime.now();
                if (!isInTradeTime(startup)) {
                    log.info("current time is {}.\n Program will exit. Thanks for your usage again.", startup);
                    break;
                }
                fillHolderInfo(null);
                List<EtfsCompareResp.Suggest> suggests = new ArrayList<>();
                List<TradeConfProperties.StrategyT0.EtfGroup> etfGroups = confProperties.getStrategyT0().getEtfGroups();
                for (TradeConfProperties.StrategyT0.EtfGroup etfGroup : etfGroups) {
                    List<EtfsCompareResp.Suggest> suggestsPart = StrategySuggestApi.getSuggestsInEtfGroup(
                        etfGroup.getCodes(),
                        confProperties.getMinMoneyPerTrade(),
                        confProperties.getMinNetProfit()
                    );
                    CollectionUtils.addAll(suggests, suggestsPart);
                }

//                try {
//                    suggests = StrategySuggestApi.getSuggests(
//                        confProperties.getMinMoneyPerTrade(),
//                        confProperties.getMinNetProfit()
//                    );
//                } catch (Exception ignored) {
//                    continue;
//                }
                log.info("suggests={}", suggests);
                for (EtfsCompareResp.Suggest suggest : suggests) {
                    boolean hasTrade = tradeBySuggest(suggest);
                    if (!hasTrade) {
                        fillAccountInfo();
                        sleep(30);
                    }
                }

                sleep(3000 + RandomUtils.nextInt(800, 2000));
            }
        }
    }


    private void fillAccountInfo() {
        AccountInfo newInfo = this.getAccountInfo();
        BigDecimal availableUseMoney = newInfo.getAvailableUseMoney();
        BigDecimal availableWithdrawMoney = newInfo.getAvailableWithdrawMoney();
        BigDecimal freezeMoney = newInfo.getFreezeMoney();
        BigDecimal stockMarketMoney = newInfo.getStockMarketMoney();
        BigDecimal totalMoney = newInfo.getTotalMoney();

        if (Objects.nonNull(availableUseMoney)) {
            accountInfo.setAvailableUseMoney(availableUseMoney);
        }
        if (Objects.nonNull(availableWithdrawMoney)) {
            accountInfo.setAvailableWithdrawMoney(availableWithdrawMoney);
        }
        if (Objects.nonNull(freezeMoney)) {
            accountInfo.setFreezeMoney(freezeMoney);
        }
        if (Objects.nonNull(stockMarketMoney)) {
            accountInfo.setStockMarketMoney(stockMarketMoney);
        }
        if (Objects.nonNull(totalMoney)) {
            accountInfo.setTotalMoney(totalMoney);
        }
    }

    protected boolean checkHolderBeforeTrade() {
        return false;
    }

    private boolean tradeBySuggest(EtfsCompareResp.Suggest suggest) {
        String codeToSell = suggest.getCodeToSell().substring(2);
        double priceToSell = suggest.getPriceToSell();
        int quantityToSell = suggest.getQuantityToSell();
        String codeToBuy = suggest.getCodeToBuy().substring(2);
        double priceToBuy = suggest.getPriceToBuy();
        int quantityToBuy = suggest.getQuantityToBuy();
        double currentPriceOfBuy = suggest.getCurrentPriceOfBuy();
        double extraSavedMoney = suggest.getExtraSavedMoney();
        double netSavedMoney = suggest.getNetSavedMoney();
        double totalFee = suggest.getTotalFee();

        BigDecimal moneyUseForBuy = BigDecimal.valueOf(priceToBuy).multiply(BigDecimal.valueOf(quantityToBuy));
        if (accountInfo.getAvailableUseMoney().compareTo(moneyUseForBuy) < 0) {
            log.warn("no enough available money: {}, but you only have {}", moneyUseForBuy, accountInfo.getAvailableUseMoney());
            return false;
        }

        HolderInfo existHolderInfo = holderInfoMap.get(codeToSell);
        if (Objects.isNull(existHolderInfo)) {
            log.info("no stock to sell with code={}", codeToSell);
            return false;
        }
        if (checkHolderBeforeTrade()) {
            int stockAvailableQuantity = existHolderInfo.getStockAvailableQuantity();
            if (stockAvailableQuantity < quantityToSell) {
                log.info("no enough quantity stock with code={}, need={}, available={}",
                    codeToSell, quantityToSell, stockAvailableQuantity);
                return false;
            }
        }

        buyAndSell(codeToSell, priceToSell, quantityToSell, codeToBuy, priceToBuy, quantityToBuy);

        return true;
    }

    /**
     * 是否是交易时间
     */
    private boolean isInTradeTime(LocalTime time) {
        return StockUtils.isInTradeTime(time);
    }


    private void buyAndSell(String codeToSell, double priceToSell, int quantityToSell,
                            String codeToBuy, double priceToBuy, int quantityToBuy) {
        if (confProperties.getFlags().isBuyFirst()) {
            log.info("try to buy with code={}, price={}, quantity={}", codeToBuy, priceToBuy, quantityToBuy);
            prepareAndBuy(codeToBuy, priceToBuy, quantityToBuy);
            log.info("try to sell with code={}, price={}, quantity={}", codeToSell, priceToSell, quantityToSell);
            prepareAndSell(codeToSell, priceToSell, quantityToSell);
        } else {
            log.info("try to sell with code={}, price={}, quantity={}", codeToSell, priceToSell, quantityToSell);
            prepareAndSell(codeToSell, priceToSell, quantityToSell);
            log.info("try to buy with code={}, price={}, quantity={}", codeToBuy, priceToBuy, quantityToBuy);
            prepareAndBuy(codeToBuy, priceToBuy, quantityToBuy);
        }
    }


    public final void prepareAndSell(String code, double price, int quantity) {
        doSell(code, price, quantity, "");
    }

    public final void prepareAndBuy(String code, double price, int quantity) {
        doBuy(code, price, quantity, "");
    }

}
