package cn.valuetodays.api2.module.fortune.client.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * etf间的对比结果.
 *
 * @author lei.liu
 * @since 2023-05-23 16:18
 */
@Data
public class EtfsCompareResp implements Serializable {
    private String groupName;
    private int tn;
    private List<Result> infos;
    private List<Suggest> suggests;

    @Data
    public static class Result implements Serializable {
        private String code;
        private Double open;
        private Double current;
        private Double avgPrice;
    }

    @Data
    public static class Suggest implements Serializable {
        private String codeToSell;
        private double priceToSell;
        private int quantityToSell;
        private String codeToBuy;
        private double priceToBuy;
        private int exceptedQuantityToBuy;
        private double exceptedPriceToBuy;
        private int quantityToBuy;
        private double currentPriceOfBuy;
        // 节省的钱数
        private double extraSavedMoney;
        // 净节省的钱数 （=节省的钱数 - 手续费）
        private double netSavedMoney;
        // 手续费
        private double totalFee;
        // 备注
        private String remark;
    }
}
