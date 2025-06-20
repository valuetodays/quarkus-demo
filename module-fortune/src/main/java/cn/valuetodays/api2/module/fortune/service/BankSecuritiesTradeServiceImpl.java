package cn.valuetodays.api2.module.fortune.service;

import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.channel.BankSecurityTradeParser;
import cn.valuetodays.api2.module.fortune.channel.BankSecurityTradeParserFactory;
import cn.valuetodays.api2.module.fortune.dao.BankSecuritiesTradeDAO;
import cn.valuetodays.api2.module.fortune.persist.BankSecuritiesTradePersist;
import cn.valuetodays.api2.module.fortune.reqresp.StatEarnedMonthlyResp;
import cn.valuetodays.api2.module.fortune.reqresp.StatEarnedResp;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * 银证交易
 *
 * @author lei.liu
 * @since 2024-08-19 17:32
 */
@ApplicationScoped
@Slf4j
public class BankSecuritiesTradeServiceImpl
    extends BaseCrudService<Long, BankSecuritiesTradePersist, BankSecuritiesTradeDAO> {
    @Inject
    SqlServiceImpl sqlService;

    public AffectedRowsResp parseTextAndSave(SimpleTypesReq req, Long currentAccountId) {
        String text = req.getText();
        BankSecurityTradeParser parser = BankSecurityTradeParserFactory.choose(text);
        if (Objects.isNull(parser)) {
            return AffectedRowsResp.empty();
        }

        List<BankSecuritiesTradePersist> listToSave = parser.parse(text);
        listToSave.forEach(e -> e.setAccountId(currentAccountId));
        List<BankSecuritiesTradePersist> saved = this.save(listToSave);
        return AffectedRowsResp.of(saved.size());
    }

    public List<StatEarnedResp> statEarned(Long currentAccountId) {
        String sql = """
                SELECT
                    channel,
                    sum(
                        CASE
                        WHEN b.direction='B2S' THEN -1*b.money
                        WHEN b.direction='S2B' THEN b.money
                        END
                    ) as money
                FROM `eblog`.`fortune_bank_securities_trade` b where b.account_id=?
                GROUP BY b.channel
            """;
        return sqlService.queryForList(sql, StatEarnedResp.class, currentAccountId);
    }

    public List<StatEarnedMonthlyResp> statEarnedMonthly(Long currentAccountId) {
        String sql = """
                SELECT
                    DATE_FORMAT(operate_time,"%Y-%m") as year_month_str,
                    sum(
                        CASE
                        WHEN b.direction='B2S' THEN -1*b.money
                        WHEN b.direction='S2B' THEN b.money
                        END
                    ) as money
                FROM `eblog`.`fortune_bank_securities_trade` b where b.account_id=?
                GROUP BY DATE_FORMAT(b.operate_time,"%Y-%m")
                ORDER BY year_month_str ASC
            """;
        return sqlService.queryForList(sql, StatEarnedMonthlyResp.class, currentAccountId);
    }

}
