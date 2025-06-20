package cn.valuetodays.api2.module.fortune.service;

import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.dao.EtfIopvDAO;
import cn.valuetodays.api2.module.fortune.enums.StockSubjectEnums;
import cn.valuetodays.api2.module.fortune.persist.EtfIopvPO;
import cn.valuetodays.api2.module.fortune.persist.StockSubjectPO;
import cn.valuetodays.quarkus.commons.base.BaseCrudService;
import cn.vt.rest.third.eastmoney.EtfClientUtils;
import cn.vt.rest.third.eastmoney.vo.IopvData;
import cn.vt.rest.third.eastmoney.vo.IopvResp;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.exception.ConstraintViolationException;

/**
 * etf的iopv
 *
 * @author lei.liu
 * @since 2024-09-16 21:42
 */
@ApplicationScoped
@Slf4j
public class EtfIopvService
    extends BaseCrudService<Long, EtfIopvPO, EtfIopvDAO> {
    @Inject
    StockSubjectServiceImpl stockSubjectService;

    public void gatherAndSave(boolean fully) {
        List<StockSubjectPO> list = stockSubjectService.findAllByType(StockSubjectEnums.Type.IOPV);
        List<String> codes = list.stream()
            .map(StockSubjectPO::getCode)
            .toList();
        for (String code : codes) {
            gatherAndSaveOne(code, fully);
        }
    }

    public void gatherAndSaveOne(String code, boolean fully) {
        int currentPage = 1;
        while (true) {
            IopvResp resp = EtfClientUtils.getIopv(code, currentPage);
            if (Objects.isNull(resp)) {
                return;
            }
            IopvData data = resp.getData();
            if (Objects.isNull(data)) {
                return;
            }
            List<IopvData.IopvItem> list = data.getList();
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            saveListToMysql(code, list);
            if (!fully) {
                return;
            }
            currentPage++;
        }

    }

    private void saveListToMysql(String code, List<IopvData.IopvItem> list) {
        for (IopvData.IopvItem iopvItem : list) {
            EtfIopvPO etfIopvPO = new EtfIopvPO();
            etfIopvPO.setCode(code);
            etfIopvPO.setStatDate(
                DateUtils.getDate(iopvItem.getDateStr(), DateUtils.DEFAULT_DATE_FORMAT).toLocalDate()
            );
            etfIopvPO.setIopv(iopvItem.getDwjz());
            try {
                getRepository().persist(etfIopvPO);
            } catch (ConstraintViolationException ignored) {
                // 数据已重复了，就不再处理了。
            } catch (Exception e) {
                log.error("error when gatherAndSave({})", code, e);
            }
        }
    }

}
