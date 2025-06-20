package cn.valuetodays.api2.module.fortune.controller;

import java.util.Arrays;
import java.util.List;

import cn.valuetodays.api2.module.fortune.persist.EtfT0DailyInfoPersist;
import cn.valuetodays.api2.module.fortune.reqresp.T0DailyChartReq;
import cn.valuetodays.api2.module.fortune.reqresp.T0DailyChartResp;
import cn.valuetodays.api2.module.fortune.service.EtfT0DailyInfoService;
import cn.valuetodays.quarkus.commons.base.BaseCrudController;
import cn.vt.vo.StringValueLabelVO;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * t0型etf每日数据服务
 *
 * @author lei.liu
 * @since 2025-01-04 11:44
 */
@Path("/etfT0DailyInfo")
public class EtfT0DailyInfoController
    extends BaseCrudController<Long, EtfT0DailyInfoPersist, EtfT0DailyInfoService> {
    @Path(value = "/refresh.do")
    @POST
//    @Async
    public void refresh() {
        getService().refresh();
    }

    @Path(value = "/fixTotalSharesInShBySql.do")
    @POST
//    @Async
    public void fixTotalSharesInShBySql(SimpleTypesReq req) {
        Long lastId = req.getL();
        getService().fixTotalSharesInShBySql(ObjectUtils.defaultIfNull(lastId, 0L));
    }

    @Path(value = "/fixTotalShares.do")
    @POST
//    @Async
    public void fixTotalShares() {
        getService().fixTotalShares();
    }

    @Path(value = "/fixTotalSharesByCode.do")
    @POST
    public String fixTotalSharesByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return "NULL code";
        }
        getService().fixTotalSharesByCode(code);
        return "OK";
    }


    @Path(value = "/getMetricList.do")
    @POST
    public List<StringValueLabelVO> getMetricList() {
        return Arrays.stream(T0DailyChartReq.MetricType.values())
            .map(e -> StringValueLabelVO.of(e.name(), e.getTitle()))
            .toList();
    }

    @Path(value = "/dailyChart.do")
    @POST
    public List<T0DailyChartResp> dailyChart(T0DailyChartReq req) {
        return getService().dailyChart(req);
    }

}
