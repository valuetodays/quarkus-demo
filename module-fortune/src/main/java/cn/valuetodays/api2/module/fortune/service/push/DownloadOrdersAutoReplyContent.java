package cn.valuetodays.api2.module.fortune.service.push;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import cn.valuetodays.api2.module.fortune.controller.QmtController;
import cn.valuetodays.api2.web.basic.push.vocechat.AutoReplyContent;
import cn.valuetodays.api2.web.basic.push.vocechat.PushBaseReq;
import cn.vt.fortune.modestep.client.vo.OrderVo;
import cn.vt.util.DateUtils;
import cn.vt.util.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-11-28
 */
@Slf4j
@ApplicationScoped
public class DownloadOrdersAutoReplyContent implements AutoReplyContent {
    @Inject
    private QmtController qmtController;

    @Override
    public List<String> title() {
        return List.of("下载委托");
    }

    @Override
    public Pair<PushBaseReq.ContentType, String> replyContent(String value) {
        File file = writeDataToTempFile(qmtController.getSuccessOrders());
        if (Objects.isNull(file)) {
            return AutoReplyContent.makePlainText("下载委托失败了");
        }
        return AutoReplyContent.makeFile(file.getAbsolutePath());
    }

    private File writeDataToTempFile(List<OrderVo> orders) {
        if (Objects.isNull(orders)) {
            return null;
        }
        try {
            Path tempFile = Files.createTempFile("qmt-order-" + DateUtils.formatDateToday() + "-", ".json");
            File file = tempFile.toFile();
            FileUtils.writeStringToFile(file, JsonUtils.toJsonString(orders), StandardCharsets.UTF_8);
            return file;
        } catch (Exception e) {
            log.error("error when writeDataToTempFile()", e);
        }
        return null;
    }
}
