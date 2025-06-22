package cn.valuetodays.api2.module.fortune.service.push;

import java.math.BigDecimal;
import java.util.List;

import cn.valuetodays.api2.module.fortune.client.reqresp.StockPriceGradientReq;
import cn.valuetodays.api2.module.fortune.client.reqresp.StockPriceGradientResp;
import cn.valuetodays.api2.module.fortune.controller.StockUtilController;
import cn.valuetodays.api2.web.basic.push.vocechat.AutoReplyContent;
import cn.valuetodays.api2.web.basic.push.vocechat.PushBaseReq;
import cn.vt.R;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-06-15
 */
@ApplicationScoped
public class StockPriceGradientAutoReplyContent implements AutoReplyContent {
    @Inject
    StockUtilController stockUtilController;

    @Override
    public List<String> title() {
        return List.of("StockPriceGradient", "股票价格梯度");
    }

    @Override
    public Pair<PushBaseReq.ContentType, String> replyContent(String value) {
        StockPriceGradientReq req = new StockPriceGradientReq();
        if (!StringUtils.contains(value, "#")) {
            value = value + "#2";
        }
        String[] arr = StringUtils.split(value, "#");
        req.setCode(arr[0].strip());
        req.setRangeValue(new BigDecimal(arr[1].strip()));
        R<StockPriceGradientResp> r = stockUtilController.stockPriceGradient(req);
        StockPriceGradientResp data = r.getData();
        String code = data.getCode();
        BigDecimal currentPrice = data.getCurrentPrice();
        List<StockPriceGradientResp.GradientItem> gradients = data.getGradients();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---").append(code).append(" @ ").append(currentPrice);
        for (StockPriceGradientResp.GradientItem gradient : gradients) {
            stringBuilder.append("\n  ").append(gradient.getChgPtg()).append("%  ").append(gradient.getPrice());
        }
        return AutoReplyContent.makePlainText(stringBuilder.toString());
    }
}
