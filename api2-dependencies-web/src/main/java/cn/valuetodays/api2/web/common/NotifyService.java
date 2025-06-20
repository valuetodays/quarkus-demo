package cn.valuetodays.api2.web.common;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-20
 */
public interface NotifyService {
    void notifyStockMinutePrice();

    void notifyNoCookie(String domain);
}
