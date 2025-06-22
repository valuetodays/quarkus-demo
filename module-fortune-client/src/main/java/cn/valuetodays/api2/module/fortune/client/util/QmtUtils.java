package cn.valuetodays.api2.module.fortune.client.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import cn.valuetodays.api2.module.fortune.client.reqresp.DealedOrderInfo;
import cn.vt.util.DateUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-12-27
 */
public class QmtUtils {
    private QmtUtils() {
    }

    public static LocalDateTime orderTimeToLdt(long timeInSeconds) {
        return DateUtils.fromTimestamp(timeInSeconds * 1000);
    }

    public static String formatOrderUniqueId(DealedOrderInfo orderInfo) {
        Integer currentDate = formatOrderDateAsYyyyMMdd(orderInfo.getOrder_time());
        // order_id和order_sysid在多天内单独来看都不是唯一的，但合在一起再加上日期是唯一的
        // 订单编号在海通服务端重启后会变成空。
        return currentDate + orderInfo.getOrder_sysid();
    }

    public static Integer formatOrderDateAsYyyyMMdd(long timeInSeconds) {
        LocalDateTime localDateTime = orderTimeToLdt(timeInSeconds);
        LocalDate localDate = localDateTime.toLocalDate();
        return DateUtils.formatAsYyyyMMdd(localDate);
    }

}
