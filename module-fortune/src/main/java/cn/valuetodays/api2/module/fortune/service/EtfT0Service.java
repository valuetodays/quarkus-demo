package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.EtfT0DAO;
import cn.valuetodays.api2.module.fortune.persist.EtfT0PO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.jisilu.JisiluEtfClientUtils;
import cn.vt.rest.third.jisilu.vo.EtfResp;
import cn.vt.rest.third.sse.SseEtfClientUtils;
import cn.vt.rest.third.sse.vo.FundListResp;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-01-02
 */
@ApplicationScoped
@Slf4j
public class EtfT0Service extends BaseCrudService<Long, EtfT0PO, EtfT0DAO> {


    public void refresh() {
        // 获取沪市t0
        doIgnoreException(() -> saveOrUpdateAll(SseEtfClientUtils.getGoldenEtfList()));
        doIgnoreException(() -> saveOrUpdateAll(SseEtfClientUtils.getQdiiEtfList()));
        // 暂时无法获取深市t0，没有分类，https://www.szse.cn/market/product/list/etfList/index.html
        // 获取集思录t0
        doIgnoreException(() -> saveOrUpdateAll(JisiluEtfClientUtils.getEtfForAsia()));
        doIgnoreException(() -> saveOrUpdateAll(JisiluEtfClientUtils.getEtfForEurope()));
        doIgnoreException(() -> saveOrUpdateAll(JisiluEtfClientUtils.getEtfForCommodity()));
        doIgnoreException(() -> saveOrUpdateAll(JisiluEtfClientUtils.getEtfForGold()));
    }

    private void saveOrUpdateAll(List<FundListResp.FundListItem> t0EtfList) {
        for (FundListResp.FundListItem it : t0EtfList) {
            String fundCode = it.getFundCode();
            EtfT0PO old = getRepository().findByCode(fundCode);
            if (Objects.isNull(old)) {
                old = new EtfT0PO();
                old.setCode(fundCode);
                old.setName(it.getFundAbbr());
                old.initUserIdAndTime(1L);
            }
            old.setReleaseDate(Integer.valueOf(it.getListingDate()));
            old.setTotalShares(BigDecimal.ZERO);
            old.setTradeAmount(BigDecimal.ZERO);
            old.setHuanShouPtg(BigDecimal.ZERO);
            old.setYiJiaPtg(BigDecimal.ZERO);
            old.setManageRadio(BigDecimal.ZERO);
            old.setHolderRadio(BigDecimal.ZERO);
            old.setFollowIndex(it.getIndexName());
            getRepository().persist(old);
        }
    }

    private void saveOrUpdateAll(EtfResp etfResp) {
        List<EtfResp.Row> rows = etfResp.getRows();
        for (EtfResp.Row row : rows) {
            EtfResp.Cell cell = row.getCell();
            String fundId = cell.getFund_id();
            EtfT0PO old = getRepository().findByCode(fundId);
            if (Objects.isNull(old)) {
                old = new EtfT0PO();
                old.setCode(fundId);
                old.setName(cell.getFund_nm());
                old.initUserIdAndTime(1L);
            }
            old.setTotalShares(cell.getAmount());
            old.setTradeAmount(cell.getVolume());
            old.setHuanShouPtg(removePtg(cell.getTurnover_rt()));
            old.setYiJiaPtg(removePtg(cell.getDiscount_rt()));
            old.setManageRadio(cell.getM_fee());
            old.setHolderRadio(cell.getT_fee());
            getRepository().persist(old);
        }
    }

    private BigDecimal removePtg(String text) {
        if (StringUtils.equalsAny(text, null, "-", "")) {
            return BigDecimal.ZERO;
        }
        String replaced = StringUtils.replace(text, "%", "");
        return new BigDecimal(replaced);
    }


}
