package cn.valuetodays.api2.module.fortune.component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.reqresp.SaveUpStockReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.SaveUpStockResp;
import cn.vt.exception.AssertUtils;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * 攒股.
 *
 * @author lei.liu
 * @since 2023-09-26
 */
@ApplicationScoped
public class SaveUpStockModule {

    /**
     * 给定买价和数量，计算累计增加0.001作为卖价并计算出合适的卖出数量（并攒下一点股）
     */
    public List<SaveUpStockResp.PossibleItem> doSaveUp(SaveUpStockReq req) {
        BigDecimal buyPrice = req.getBuyPrice();
        Integer buyQuantity = req.getBuyQuantity();
        SaveUpStockReq.Category category = req.getCategory();
        SaveUpStockReq.Category categoryToUse = determineCategory(category, buyPrice);
        final BigDecimal totalAmountForBuy = buyPrice.multiply(BigDecimal.valueOf(buyQuantity));
        final List<SaveUpStockResp.PossibleItem> sellStrategyList = new ArrayList<>();
        BigDecimal tempSellPrice = buyPrice;
        for (int i = 0; i < 10000; i++) {
            tempSellPrice = tempSellPrice.add(categoryToUse.getOffsetValue());
            BigDecimal offsetAmount = tempSellPrice.subtract(buyPrice).multiply(BigDecimal.valueOf(buyQuantity));
            BigDecimal offsetQuantity = offsetAmount.divide(tempSellPrice, 3, RoundingMode.UP);
            // 数量差值小于100股就不用卖了
            if (offsetQuantity.compareTo(BigDecimal.valueOf(100)) < 0) {
                continue;
            }
            // 取整100的倍数
            int offsetQuantityAsInt = ((int) (offsetQuantity.doubleValue() / 100)) * 100;
            SaveUpStockResp.PossibleItem ss = new SaveUpStockResp.PossibleItem();
            ss.setPrice(tempSellPrice);
            ss.setQuantity(buyQuantity - offsetQuantityAsInt);
            ss.setAmount(tempSellPrice.multiply(BigDecimal.valueOf(ss.getQuantity())));
            ss.setCashEarned(ss.getAmount().subtract(totalAmountForBuy));
            ss.setSaveUpQuantity(offsetQuantityAsInt);
            ss.setMarketValueForSaveUpQuantity(tempSellPrice.multiply(BigDecimal.valueOf(offsetQuantityAsInt)));
            sellStrategyList.add(ss);
            if (sellStrategyList.size() > 20) {
                break;
            }
        }
        return sellStrategyList;
    }

    private SaveUpStockReq.Category determineCategory(SaveUpStockReq.Category category, BigDecimal buyPrice) {
        if (Objects.isNull(category) || SaveUpStockReq.Category.AUTO == category) {
            int scale = buyPrice.scale();
            if (scale <= 2) {
                return SaveUpStockReq.Category.STOCK;
            } else if (scale == 3) {
                return SaveUpStockReq.Category.ETF;
            } else {
                throw AssertUtils.create("买入金额有误，可以有1/2/3位小数");
            }
        }
        return category;
    }


}
