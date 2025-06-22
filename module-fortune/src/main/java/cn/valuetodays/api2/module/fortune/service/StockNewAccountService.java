package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.client.persist.StockNewAccountPersist;
import cn.valuetodays.api2.module.fortune.dao.StockNewAccountRepository;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.sse.SseNewStockAccountClientUtils;
import cn.vt.rest.third.sse.vo.StockNewAccountResp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-04-24
 */
@ApplicationScoped
@Slf4j
public class StockNewAccountService extends BaseCrudService<Long, StockNewAccountPersist, StockNewAccountRepository> {


    @Transactional
    public void refresh() {
        refresh(YearMonth.now().minusMonths(1L));
    }

    /**
     * @param yearMonth 注意要小于等于上月
     */
    @Transactional
    public void refresh(YearMonth yearMonth) {
        String yyyyMM = yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StockNewAccountResp resp = SseNewStockAccountClientUtils.getStockNewAccount(yyyyMM);
        List<StockNewAccountResp.Item> list = resp.getResult();
        for (StockNewAccountResp.Item item : list) {
            StockNewAccountPersist p = fromItem(item, yyyyMM);
            // 当月数据还未更新时就不需要处理后面的数据了
            if (Objects.isNull(p)) {
                return;
            }
            try {
                getRepository().persist(p);
//            } catch (DuplicateKeyException ignored) {
            } catch (ConstraintViolationException ignored) {
                // fall through
            } catch (Exception e) {
                log.error("error when insert ", e);
            }
        }

    }

    private StockNewAccountPersist fromItem(StockNewAccountResp.Item item, String yyyyMM) {
        BigDecimal aAccount = item.getAAccount();
        // 为0时说明当月数据还未更新
        if (BigDecimal.ZERO.compareTo(aAccount) >= 0) {
            return null;
        }
        StockNewAccountPersist p = new StockNewAccountPersist();
        p.setAAccount(aAccount);
        p.setBAccount(item.getBAccount());
        p.setFundAccount(item.getFundAccount());
        String yearMonth = item.getYearMonth();
        String yearMonthToUse = yearMonth;
        if (StringUtils.contains(yearMonth, "累计总户数")) {
            String yearStr = yyyyMM.substring(0, 4);
            yearMonthToUse = yearStr + "99"; // 累计总户数
        }
        if (StringUtils.contains(yearMonth, "年合计")) {
            yearMonthToUse = yearMonth.replace("年合计", "") + "15";
        }
        if (StringUtils.contains(yearMonth, ".")) {
            yearMonthToUse = yearMonth.replace(".", "");
        }

        p.setYearMonthVal(Integer.valueOf(yearMonthToUse));
        p.initUserIdAndTime(1L);
        return p;
    }
}
