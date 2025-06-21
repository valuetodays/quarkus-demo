package cn.valuetodays.api2.module.fortune.service.module;

import java.math.BigDecimal;

import cn.vt.rest.third.eastmoney.QuoteEnums;
import cn.vt.rest.third.eastmoney.vo.StockDetailVO;
import cn.vt.util.HttpClient4Utils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-07-27
 */
@ApplicationScoped
@Slf4j
public class TencentStockModule {

    public StockDetailVO getStockDetail(QuoteEnums.Region region, String stockCode) {
        String url = "https://qt.gtimg.cn/q=" + region.shortCode() + stockCode;
        String responseString = HttpClient4Utils.doGet(url);
        log.info("responseString={}", responseString);
        String preStr = "=\"";
        String lastStr = "\";";
        int beginInx = responseString.indexOf(preStr);
        int endInx = responseString.lastIndexOf(lastStr);
        String line = responseString.substring(beginInx + preStr.length(), endInx);
        return from(line);
    }

    public StockDetailVO from(String line) {
        String[] array = StringUtils.splitByWholeSeparator(line, "~");

        StockDetailVO vo = new StockDetailVO();
        vo.setCode(array[2]);
        vo.setName(array[1]);
        // 无法获取到平均价
        vo.setPrice(new BigDecimal(array[3]));
        vo.setAvgPrice(new BigDecimal(0));
        vo.setWaiPan(new BigDecimal(0));
        vo.setNeiPan(new BigDecimal(0));
        return vo;
    }
}
