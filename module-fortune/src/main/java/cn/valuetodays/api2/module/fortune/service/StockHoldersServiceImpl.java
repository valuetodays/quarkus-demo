package cn.valuetodays.api2.module.fortune.service;

import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.channel.StockHoldersInfo;
import cn.valuetodays.api2.module.fortune.channel.StockHoldersParser;
import cn.valuetodays.api2.module.fortune.channel.StockHoldersParserFactory;
import cn.valuetodays.api2.module.fortune.client.persist.StockHoldersPO;
import cn.valuetodays.api2.module.fortune.client.reqresp.AccountProfitVo;
import cn.valuetodays.api2.module.fortune.client.reqresp.GetAccountProfitReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.HolderInfo;
import cn.valuetodays.api2.module.fortune.client.reqresp.SaveAllHoldersReq;
import cn.valuetodays.api2.module.fortune.dao.StockHoldersDAO;
import cn.valuetodays.api2.web.common.AffectedRowsResp;
import cn.valuetodays.api2.web.common.SqlServiceImpl;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.exception.AssertUtils;
import cn.vt.moduled.fortune.enums.FortuneCommonEnums;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 我的证券账户持有
 *
 * @author lei.liu
 * @since 2023-01-10 21:38
 */
@ApplicationScoped
@Slf4j
public class StockHoldersServiceImpl
    extends BaseCrudService<Long, StockHoldersPO, StockHoldersDAO> {
    @Inject
    SqlServiceImpl sqlService;


    @Transactional
    public int saveAllHolders(SaveAllHoldersReq req, Long currentAccountId) {
        List<HolderInfo> holderInfos = req.getHolderInfos();
        if (CollectionUtils.isEmpty(holderInfos)) {
            return 0;
        }
        int date = req.getDate();
        if (date < 20200101) {
            return 0;
        }
        FortuneCommonEnums.Channel channel = req.getChannel();
        List<StockHoldersPO> listToSave = holderInfos.stream().map(e -> {
            StockHoldersPO mshp = new StockHoldersPO();
            mshp.setChannel(channel);
            mshp.setStatDate(date);
            mshp.setSecCode(e.getCode());
            mshp.setHoldVolume(e.getStockQuantity());
            mshp.setCostPrice(e.getCostPrice());
            mshp.setMarketPrice(e.getMarketPrice());
            mshp.setAccountId(currentAccountId);
            return mshp;
        }).toList();
        return toSaveIfNecessary(listToSave);
    }

    @Transactional
    public AffectedRowsResp parseTextAndSave(SimpleTypesReq req, Long currentAccountId) {
        String holderText = req.getText();
        Integer statDate = req.getI();
        AssertUtils.assertStringNotBlank(holderText, "持仓信息【holderText】不能为空");
        AssertUtils.assertNotNull(statDate, "统计日期【i】不能为空");

        StockHoldersParser parser = StockHoldersParserFactory.choose(holderText);
        if (Objects.isNull(parser)) {
            return AffectedRowsResp.empty();
        }
        List<StockHoldersInfo> holdersInfoList = parser.parse(holderText, "\n");
        SaveAllHoldersReq reqToUse = new SaveAllHoldersReq();
        reqToUse.setDate(statDate);
        reqToUse.setChannel(parser.channel());
        List<HolderInfo> poList = holdersInfoList.stream().map(e -> {
            HolderInfo po = new HolderInfo();
            po.setCode(e.getSecCode());
            po.setName(e.getSecAbbr());
            po.setStockQuantity(e.getHoldVolume());
            po.setStockAvailableQuantity(e.getHoldVolume());
            po.setCostPrice(e.getCostPrice());
            po.setMarketPrice(e.getMarketPrice());
            po.setShareAccount("-");
            return po;
        }).toList();
        reqToUse.setHolderInfos(poList);
        return AffectedRowsResp.of(this.saveAllHolders(reqToUse, currentAccountId));
    }

    public List<StockHoldersPO> getCostPrice(SimpleTypesReq req) {
        String text = req.getText();
        AssertUtils.assertStringNotBlank(text, "编号【text】不能为空");
        return getRepository().findAllBySecCodeOrderByStatDateAsc(text);
    }


        public List<AccountProfitVo> getAccountProfit(GetAccountProfitReq req) {
            Long currentAccountId = req.getAccountId();
            String sqlWithVar = """
                SELECT
                    sum((msh.market_price-msh.cost_price) * msh.hold_volume) as profit,
                    msh.stat_date as statDate
                FROM `fortune_stock_holders` msh
                where
                    market_price>0
                    and account_id=?
                    {{_CHANNEL_CONDITION_}}
                group by statDate
                order by statDate asc
                """;
            String replacement = "";
            FortuneCommonEnums.Channel channel = req.getChannel();
            if (FortuneCommonEnums.Channel.ALL != channel) {
                replacement = " AND channel='" + channel.name() + "' ";
            }
            sqlWithVar = StringUtils.replace(sqlWithVar, "{{_CHANNEL_CONDITION_}}", replacement);
            return sqlService.queryForList(sqlWithVar, AccountProfitVo.class, currentAccountId);
//            return jdbcTemplate.query(sqlWithVar, new BeanPropertyRowMapper<>(AccountProfitVo.class), currentAccountId);
        }

    private int toSaveIfNecessary(List<StockHoldersPO> listToSave) {
        if (CollectionUtils.isEmpty(listToSave)) {
            return 0;
        }
        int n = 0;
        for (StockHoldersPO toSave : listToSave) {
            StockHoldersPO old = getRepository().findByChannelAndStatDateAndSecCode(
                toSave.getChannel(), toSave.getStatDate(), toSave.getSecCode()
            );
            if (Objects.nonNull(old)) {
                old.setHoldVolume(toSave.getHoldVolume());
                old.setCostPrice(toSave.getCostPrice());
                old.setMarketPrice(toSave.getMarketPrice());
                this.save(old);
                n++;
            } else {
                try {
                    getRepository().persist(toSave);
                    n++;
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        }
        return n;

    }
}
