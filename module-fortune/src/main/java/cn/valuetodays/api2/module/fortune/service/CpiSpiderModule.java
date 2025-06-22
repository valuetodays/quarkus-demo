package cn.valuetodays.api2.module.fortune.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.persist.CpiPO;
import cn.valuetodays.api2.module.fortune.client.reqresp.CpiResp;
import cn.valuetodays.api2.module.fortune.dao.CpiDAO;
import cn.vt.util.DateUtils;
import cn.vt.util.HttpClient4Utils;
import cn.vt.util.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @see <a href="https://data.eastmoney.com/cjsj/cpi.html">eastmoney-cpi</a>
 * @since 2023-01-13
 */
@ApplicationScoped
@Slf4j
public class CpiSpiderModule {

    @Inject
    private CpiDAO cpiDAO;

    public void refreshCpiData() {
        int pn = 1;
        int size = 200;
        while (true) {
            int pages = doSpiderAndSaveToMysql(pn, size);
            if (pn >= pages) {
                break;
            }
            pn++;
        }
    }

    private int doSpiderAndSaveToMysql(int pn, int size) {
        String apiUrl = "https://datacenter-web.eastmoney.com/api/data/v1/get?"
            + "callback=datatable6520856"
            + "&columns=REPORT_DATE%2CTIME%2CNATIONAL_SAME%2CNATIONAL_BASE%2CNATIONAL_SEQUENTIAL%2CNATIONAL_ACCUMULATE%2CCITY_SAME%2CCITY_BASE%2CCITY_SEQUENTIAL%2CCITY_ACCUMULATE%2CRURAL_SAME%2CRURAL_BASE%2CRURAL_SEQUENTIAL%2CRURAL_ACCUMULATE"
            + "&pageNumber=" + pn
            + "&pageSize=" + size
            + "&sortColumns=REPORT_DATE"
            + "&sortTypes=-1"
            + "&source=WEB"
            + "&client=WEB"
            + "&reportName=RPT_ECONOMY_CPI"
            + "&p=" + pn
            + "&pageNo=" + pn
            + "&pageNum=" + pn
            + "&_=" + System.currentTimeMillis();

        String respStr = HttpClient4Utils.doGet(apiUrl);
        int inx = respStr.indexOf("({");
        int endInx = respStr.indexOf("});", inx);
        String substring = respStr.substring(inx + "({".length(), endInx);
        String jsonStr = "{" + substring + "}";
        CpiResp cpiResp = JsonUtils.fromJson(jsonStr, CpiResp.class);
        CpiResp.CpiDataObj cpiDataObj = cpiResp.getResult();
        List<CpiResp.CpiDataObj.ValueObj> datas = cpiDataObj.getData();
        List<CpiPO> cpiPoList = datas.stream()
            .map(e -> {
                CpiPO cpiPO = new CpiPO();
                cpiPO.setStatYearMonth(
                    Integer.valueOf(DateUtils.formatDate(DateUtils.fromDate(e.getReportDate()), "yyyyMM"))
                );
                cpiPO.setNationalAccumulate(format(e.getNationalAccumulate()));
                cpiPO.setNationalBase(format(e.getNationalBase()));
                cpiPO.setCityAccumulate(format(e.getCityAccumulate()));
                cpiPO.setCityBase(format(e.getCityBase()));
                cpiPO.setRuralAccumulate(format(e.getRuralAccumulate()));
                cpiPO.setRuralBase(format(e.getRuralBase()));
                cpiPO.initUserIdAndTime(1L);
                return cpiPO;
            }).toList();

        final List<Integer> oldYearMonthList = cpiDAO.findAllYearMonth();
        List<CpiPO> toSaveList = cpiPoList.stream()
            .filter(e -> !oldYearMonthList.contains(e.getStatYearMonth()))
            .toList();

        for (CpiPO cpiPO : toSaveList) {
            try {
                cpiDAO.persist(cpiPO);
            } catch (Exception e) {
                log.error("error ", e);
            }
        }
        return cpiDataObj.getPages();
    }

    private Double format(Double value) {
        // BigDecimal.ROUND_UP 四舍五入
        // BigDecimal.ROUND_DOWN 直接舍弃保留位数之后小数
        BigDecimal bigDecimal = BigDecimal.valueOf(value).setScale(2, RoundingMode.DOWN);
        return bigDecimal.doubleValue();
    }

}
