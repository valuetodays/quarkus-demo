package cn.valuetodays.api2.module.fortune.service;

import cn.valuetodays.api2.module.fortune.client.persist.FundTradeMonthlyStatPO;
import cn.valuetodays.api2.module.fortune.dao.FundTradeMonthlyStatDAO;
import cn.valuetodays.api2.module.fortune.service.module.SSEFundTradeMonthlyModule;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-30
 */
@Slf4j
@ApplicationScoped
public class FundTradeMonthlyStatServiceImpl
    extends BaseCrudService<Long, FundTradeMonthlyStatPO, FundTradeMonthlyStatDAO> {

    @Inject
    SSEFundTradeMonthlyModule sseFundTradeMonthlyModule;

}
