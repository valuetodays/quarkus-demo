package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.channel.huabao.HuabaoTradeInterestParser;
import cn.valuetodays.api2.module.fortune.client.persist.StockTradeInterestPO;
import cn.valuetodays.api2.module.fortune.dao.StockTradeInterestDAO;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.exception.AssertUtils;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-10-17
 */
@Slf4j
@ApplicationScoped
public class StockTradeInterestServiceImpl
    extends BaseCrudService<Long, StockTradeInterestPO, StockTradeInterestDAO> {

    @Inject
    SqlServiceImpl sqlService;

    public BigDecimal sumAllInterest() {
        return getRepository().sumAllInterest();
    }

    public AffectedRowsResp parseTextAndSave(SimpleTypesReq req) {
        String text = req.getText();
        AssertUtils.assertStringNotBlank(text, "资金流水【text】不能为空");

        List<String> sqlList = HuabaoTradeInterestParser.parse(text);
        return sqlService.saveBySqls(sqlList);
    }

}
