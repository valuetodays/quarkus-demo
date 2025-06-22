package cn.valuetodays.api2.module.fortune.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.valuetodays.api2.module.fortune.client.persist.EtfGroupDetailPO;
import cn.valuetodays.api2.module.fortune.client.persist.EtfGroupPO;
import cn.valuetodays.api2.module.fortune.client.reqresp.EtfGroupAndDetailVo;
import cn.valuetodays.api2.module.fortune.dao.EtfGroupDAO;
import cn.valuetodays.api2.module.fortune.dao.EtfGroupDetailDAO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;

/**
 * etf组
 *
 * @author lei.liu
 * @since 2023-05-31 18:03
 */
@ApplicationScoped
public class EtfGroupServiceImpl
    extends BaseCrudService<Long, EtfGroupPO, EtfGroupDAO> {

    @Inject
    EtfGroupDetailDAO etfGroupDetailDAO;
    @Inject
    RedisDataSource stringRedisTemplate;

    public List<EtfGroupAndDetailVo> getAllEtfGroupAndDetail() {
        return getAllEtfGroupAndDetail0(-1);
    }

    public List<EtfGroupAndDetailVo> getAllTnEtfGroupAndDetail(int tn) {
        return getAllEtfGroupAndDetail0(tn);
    }

    private List<EtfGroupAndDetailVo> getAllEtfGroupAndDetail0(int tn) {
        List<EtfGroupPO> all;
        if (tn < 0) {
            all = getRepository().findAllByEnableFlag(true);
        } else {
            all = getRepository().findAllByEnableFlagAndTn(true, tn);
        }
        if (CollectionUtils.isEmpty(all)) {
            return List.of();
        }
        Map<Long, List<EtfGroupDetailPO>> detailsMap = new HashMap<>();
        List<EtfGroupDetailPO> details = etfGroupDetailDAO.listAll();
        if (CollectionUtils.isNotEmpty(details)) {
            detailsMap = details.stream().collect(Collectors.groupingBy(EtfGroupDetailPO::getGroupId));
        }
        final Map<Long, List<EtfGroupDetailPO>> finalDetailsMap = detailsMap;
        return all.stream().map(e -> {
            EtfGroupAndDetailVo egad = new EtfGroupAndDetailVo();
            egad.setName(e.getName());
            egad.setTn(e.getTn());
            List<EtfGroupDetailPO> detailListInGroupId = finalDetailsMap.get(e.getId());
            egad.setCodes(
                detailListInGroupId.stream()
                    .map(EtfGroupDetailPO::getCode)
                    .collect(Collectors.toList())
            );
            return egad;
        }).toList();
    }

}
