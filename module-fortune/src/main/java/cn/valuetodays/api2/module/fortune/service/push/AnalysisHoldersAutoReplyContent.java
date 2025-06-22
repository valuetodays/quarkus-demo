package cn.valuetodays.api2.module.fortune.service.push;

import java.util.List;

import cn.valuetodays.api2.module.fortune.controller.QmtController;
import cn.valuetodays.api2.web.basic.push.vocechat.AutoReplyContent;
import cn.valuetodays.api2.web.basic.push.vocechat.PushBaseReq;
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
public class AnalysisHoldersAutoReplyContent implements AutoReplyContent {
    @Inject
    private QmtController qmtController;

    @Override
    public List<String> title() {
        return List.of("分析持仓");
    }

    @Override
    public Pair<PushBaseReq.ContentType, String> replyContent(String value) {
        return AutoReplyContent.makePlainText(qmtController.analysisHoldersAsString());
    }
}
