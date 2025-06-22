package cn.valuetodays.api2.basic.controller;

import cn.valuetodays.api2.basic.NatsConstants;
import cn.valuetodays.api2.web.common.IVtNatsClient;
import cn.valuetodays.quarkus.commons.base.RunAsync;
import cn.vt.util.AesUtil;
import cn.vt.web.req.SimpleTypesReq;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-12-16
 */
@Path("/nats")
@Slf4j
public class NatsController extends RunAsync {

    @Inject
    private IVtNatsClient vtNatsClient;

    @Path("/anon/publish.do")
    @POST
    public void publish(SimpleTypesReq msg) {
        super.executeAsync(() -> {
            String text = msg.getText();
            if (StringUtils.isNotBlank(text)) {
                String decrypt = AesUtil.decrypt("Z2za2DaeqeeJgbk8", text);
                vtNatsClient.publish(NatsConstants.Topic.TOPIC_MODESTEP_TRADE, decrypt);
            }
        });
    }
}
