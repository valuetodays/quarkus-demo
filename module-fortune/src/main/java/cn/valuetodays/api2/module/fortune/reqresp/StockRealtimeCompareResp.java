package cn.valuetodays.api2.module.fortune.reqresp;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-02-08
 */
@Data
public class StockRealtimeCompareResp implements Serializable {
    public List<ItemResp> items;

    public static ItemResp ofItem(String code1, String code2, String suggest) {
        ItemResp item = new ItemResp();
        item.setCode1(code1);
        item.setCode2(code2);
        item.setSuggest(suggest);
        return item;
    }

    @Data
    public static class ItemResp implements Serializable {
        private String code1;
        private String code2;
        private EtfsRealtimeEtfsQuoteResp info1;
        private EtfsRealtimeEtfsQuoteResp info2;
        private double offsetPercentSet;
        private double offsetPercentReal;
        private String suggest;
    }
}
