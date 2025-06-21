package cn.valuetodays.api2.web.module.fortune.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.StockTradeDayPO;
import cn.valuetodays.api2.module.fortune.service.StockTradeDayService;
import cn.vt.util.DateUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests for {@link StockTradeDayService}.
 *
 * @author lei.liu
 * @since 2024-05-01
 */
@Slf4j
@EnabledOnOs(OS.WINDOWS)
class StockTradeDayServiceMainTest {

    @Inject
    private StockTradeDayService stockTradeDayService;

    @Test
    void save() {
        // get data from https://www.tdx.com.cn/url/holiday/
        List<Integer> notOpenDays = List.of(
            20250101, 20250128, 20250129, 20250130, 20250131, 20250203,
            20250204, 20250404, 20250501, 20250502, 20250505, 20250602, 20251001,
            20251002, 20251003, 20251006, 20251007, 20251008);
        List<DayOfWeek> weekends = List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        List<Integer> openDays = new ArrayList<>(286);
        LocalDate date = LocalDate.of(2025, 1, 1);
        while (date.isBefore(LocalDate.of(2026, 1, 1))) {
            String dateStr = DateUtils.formatDate(date.atStartOfDay(), "yyyyMMdd");
            int dateAsInt = Integer.parseInt(dateStr);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (!weekends.contains(dayOfWeek) && !notOpenDays.contains(dateAsInt)) {
                openDays.add(dateAsInt);
            }
            date = date.plusDays(1);
        }
        StockTradeDayPO saved = stockTradeDayService.findShangHaiByYear(2025);
        saved.setContent(openDays);
        stockTradeDayService.save(saved);
    }
}
