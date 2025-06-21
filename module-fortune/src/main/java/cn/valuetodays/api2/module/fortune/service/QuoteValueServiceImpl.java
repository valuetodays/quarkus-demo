package cn.valuetodays.api2.module.fortune.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.dao.QuoteValueDAO;
import cn.valuetodays.api2.module.fortune.persist.QuoteValuePO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.danjuan.DanjuanQuoteClientUtils;
import cn.vt.rest.third.danjuan.vo.DjIndexData;
import cn.vt.rest.third.danjuan.vo.DjIndexResp;
import cn.vt.rest.third.danjuan.vo.DjPbData;
import cn.vt.rest.third.danjuan.vo.DjPbResp;
import cn.vt.rest.third.danjuan.vo.DjPeData;
import cn.vt.rest.third.danjuan.vo.DjPeResp;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2021-08-17 16:29
 */
@Slf4j
@ApplicationScoped
public class QuoteValueServiceImpl
    extends BaseCrudService<Long, QuoteValuePO, QuoteValueDAO> {

    public List<QuoteValuePO> findAllByCode(String code) {
        return getRepository().findAllByCode(code);
    }

    public List<DjIndexData.Item> getAllIndexObjs() {
        DjIndexResp djIndexResp = DanjuanQuoteClientUtils.evaluationList();
        return djIndexResp.getData().getItems();
    }

    @Transactional
    public boolean refresh() {
        try {
            List<DjIndexData.Item> allIndexObjs = getAllIndexObjs();
            for (DjIndexData.Item indexObj : allIndexObjs) {
                String regionAndCode = indexObj.getIndexCode();
                String region = regionAndCode.substring(0, 2);
                String code = regionAndCode.substring(2);
                if (!Arrays.asList("SH", "SZ").contains(region)) {
                    continue;
                }
                List<DjPbData.IndexEvaPbGrowth> pbList = gatherPb(indexObj, region, code);
                savePe(region, code, pbList);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<DjPbData.IndexEvaPbGrowth> gatherPb(DjIndexData.Item indexObj, String region, String code) {
        DjPbResp pbJsonData = DanjuanQuoteClientUtils.pbHistory(region, code, "3y");
        return pbJsonData.getData().getPbList();
    }

    /**
     * PE百分位代表当前PE在选定区间所处的水平，
     * 假设PE百分位为10%，表示只有10%的时候比当前市盈率低。
     * 百分位基于指数近10年PE数据计算，若不满10年则采用全部历史数据，
     * PB百分位同理；
     *
     * @param region region
     * @param code   code
     * @param pbList pbList
     */
    private void savePe(String region, String code,
                        List<DjPbData.IndexEvaPbGrowth> pbList) {
        DjPeResp peJsonData = DanjuanQuoteClientUtils.peHistory(region, code, "3y");
//        List<DjPeData.HorizontalLine> horizontalLines = peJsonData.getData().getHorizontalLines();
//        horizontalLines.forEach(e ->
//            getLogger().info(
//                StringUtils.join(Arrays.asList(e.getLineName(), e.getLineValue()), ", ")
//            )
//        );
        List<DjPeData.IndexEvaPeGrowth> peList = peJsonData.getData().getPeList();
        QuoteValuePO latest = getRepository().findTop1ByCodeOrderByStatDateDesc(code);
        long maxStatDate = -1L;
        if (Objects.nonNull(latest)) {
            Integer statDate = latest.getStatDate();
            if (Objects.nonNull(statDate)) {
                maxStatDate = statDate;
            }
        }
        final long maxDateInTimestampToUse = maxStatDate;

        Map<Long, Double> tsAndPbMap = pbList.stream()
            .collect(Collectors.toMap(DjPbData.IndexEvaPbGrowth::getTs, DjPbData.IndexEvaPbGrowth::getPb));
        List<QuoteValuePO> toSaveList = peList.stream()
            .filter(e -> e.getTs() > maxDateInTimestampToUse)
            .map(e -> {
                QuoteValuePO toSave = new QuoteValuePO();
                toSave.setCode(code);
                toSave.setStatDate(DateUtils.formatAsYyyyMMdd(e.getTs()));
                toSave.setPeVal(e.getPe());
                toSave.setPbVal(tsAndPbMap.getOrDefault(e.getTs(), 0d));
                return toSave;
            })
            .toList();
        if (CollectionUtils.isNotEmpty(toSaveList)) {
            for (QuoteValuePO toSave : toSaveList) {
                try {
                    getRepository().persist(toSave);
                } catch (Exception e) {
                    log.error("error when save IndexValuePO", e);
                }
            }
        } else {
            log.info("no pe to save");
        }

    }


}
