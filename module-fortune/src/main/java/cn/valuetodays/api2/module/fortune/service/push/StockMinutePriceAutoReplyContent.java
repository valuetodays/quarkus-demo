package cn.valuetodays.api2.module.fortune.service.push;

import java.util.List;

import cn.valuetodays.api2.module.fortune.service.StockMinutePriceServiceImpl;
import cn.valuetodays.api2.web.basic.push.vocechat.AutoReplyContent;
import cn.valuetodays.api2.web.basic.push.vocechat.PushBaseReq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-12-12
 */
@ApplicationScoped
public class StockMinutePriceAutoReplyContent implements AutoReplyContent {
    @Inject
    StockMinutePriceServiceImpl stockMinutePriceService;


    @Override
    public List<String> title() {
        return List.of("分析股票分时价格分布：");
    }

    @Override
    public List<String> sampleValue() {
        return title().stream().map(e -> e + "513360").toList();
    }

    @Override
    public Pair<PushBaseReq.ContentType, String> replyContent(String value) {
        String v = stockMinutePriceService.computeOffsetDistribution(value);
        return AutoReplyContent.makePlainText(v);

    }
}
