package cn.valuetodays.api2.module.fortune.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.StockTradeDayDAO;
import cn.valuetodays.api2.module.fortune.persist.StockTradeDayPO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.StockEnums;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 股票交易日
 *
 * @author lei.liu
 * @since 2024-05-01 02:02
 */
@ApplicationScoped
@Slf4j
public class StockTradeDayService
    extends BaseCrudService<Long, StockTradeDayPO, StockTradeDayDAO> {

    public StockTradeDayPO findShangHaiByCurrentYear() {
        String yearStr = DateUtils.formatDate(LocalDateTime.now(), "yyyy");
        int year = Integer.parseInt(yearStr);
        return findShangHaiByYear(year);
    }

    public StockTradeDayPO findShangHaiByYear(int year) {
        return getRepository().findByYearAndRegion(year, StockEnums.Region.SHANGHAI);
    }

    /**
     * 指定日期是否开市
     */
    public boolean isOpenDay(LocalDate localDate) {
        // 需要注意每年同步一次数据
        String dateStr = DateUtils.formatDate(localDate.atStartOfDay(), "yyyyMMdd");
        int dateAsInt = Integer.parseInt(dateStr);
        int year = localDate.getYear();
        StockTradeDayPO po = findShangHaiByYear(year);
        if (Objects.nonNull(po)) {
            List<Integer> content = po.getContent();
            return CollectionUtils.containsAny(content, dateAsInt);
        }
        return false;
    }
}
