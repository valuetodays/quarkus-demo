package cn.valuetodays.api2.module.fortune.component;

import java.util.Arrays;
import java.util.List;

import cn.valuetodays.api2.module.fortune.component.reqresp.StockForGuXiReq;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * 吃息股.
 *
 * @author lei.liu
 * @since 2023-07-31
 */
@ApplicationScoped
@Slf4j
public class ChiXiGuModule extends BaseRongZiModule {

    @Override
    public List<StockForGuXiReq> determineStockList() {
        return Arrays.asList(
            new StockForGuXiReq("SH601088", "中国神华", 22.6),
            // https://xueqiu.com/snowman/S/SH600036/detail#/FHPS 注意是每10派
            new StockForGuXiReq("SH600036", "招商银行", 19.72),
            new StockForGuXiReq("SH601328", "交通银行", 3.75),
            new StockForGuXiReq("SH601939", "建设银行", 4),
            new StockForGuXiReq("SH601398", "工商银行", 3.064),
            new StockForGuXiReq("SH601288", "农业银行", 2.309),
            new StockForGuXiReq("SH601988", "中国银行", 2.364),
            new StockForGuXiReq("SH600919", "江苏银行", 4.907), // 2024
            // https://fundf10.eastmoney.com/fhsp_510880.html 注意是每1派，需要转成10派
            new StockForGuXiReq("SH510300", "300ETF", 0.0690 * 10), // 2024
            new StockForGuXiReq("SH515080", "中证红利招商", 0.0150 * 10 * 4),
            new StockForGuXiReq("SH515180", "中证红利易方达", 0.44), // 2023
            new StockForGuXiReq("SH510880", "上证红利华泰", 0.1310 * 10)  // 2024
        );
    }
}
