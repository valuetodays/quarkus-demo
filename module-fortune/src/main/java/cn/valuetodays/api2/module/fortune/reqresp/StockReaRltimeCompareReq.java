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
public class StockReaRltimeCompareReq implements Serializable {
    public List<Item> items;

    public static Item ofItem(String code1, String code2, double offsetPercent) {
        Item item = new Item();
        item.setCode1(code1);
        item.setCode2(code2);
        item.setOffsetPercent(offsetPercent);
        return item;
    }

    @Data
    public static class Item implements Serializable {
        private String code1;
        private String code2;
        private double offsetPercent;
    }
}
