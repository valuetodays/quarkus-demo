package cn.valuetodays.api2.module.fortune.service.push;

import java.time.LocalDateTime;
import java.util.List;

import cn.valuetodays.api2.module.fortune.service.module.EastMoneyIndexModule;
import cn.valuetodays.api2.web.basic.push.vocechat.AutoReplyContent;
import cn.valuetodays.api2.web.basic.push.vocechat.PushBaseReq;
import cn.vt.util.DateUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-11-28
 */
@ApplicationScoped
public class EastMoneyAShareRealtimeTurnAmountAutoReplyContent implements AutoReplyContent {
    @Inject
    private EastMoneyIndexModule eastMoneyIndexModule;

    @Override
    public List<String> title() {
        return List.of("A股当天成交额", "A股今天成交额", "A股当日成交额", "A股今日成交额");
    }

    @Override
    public Pair<PushBaseReq.ContentType, String> replyContent(String value) {
        LocalDateTime today = DateUtils.getToday();
        int yyyyMMdd = Integer.parseInt(DateUtils.formatDate(today).replace("-", ""));
        return AutoReplyContent.makePlainText(eastMoneyIndexModule.buildAShareRealtimeTurnAmountMsg(yyyyMMdd, null));
    }
}
