package cn.valuetodays.api2.basic.component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import cn.valuetodays.api2.basic.NatsConstants;
import cn.valuetodays.api2.basic.service.NotifyServiceImpl;
import cn.valuetodays.api2.web.module.fortune.IProcessTrade;
import io.nats.client.Dispatcher;
import io.nats.client.MessageHandler;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 *
 * @author lei.liu
 * @since 2025-05-28
 */
@ApplicationScoped
@Priority(PriorityConstant.NATS_CONSUMER_ORDER)
@Slf4j
public class NatsConsumer {
    public static final Logger LOGGER = LoggerFactory.getLogger(NatsConsumer.class);

    @Inject
    VtNatsClient vtNatsClient;
    @Inject
    NotifyServiceImpl notifyService;


    void onStartup(@Observes @Priority(PriorityConstant.NATS_CONSUMER_ORDER) StartupEvent unused) {
        int tryTimes = 1;
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
            log.info("sleep 1s");
            if (vtNatsClient.isConnected()) {
                log.info("natsClient is ready");
                break;
            }
            if (tryTimes > 30) {
                log.warn("try 30 times for natsClient to be ready");
                break;
            }
            tryTimes++;
        }

        Consumer<Dispatcher> consumer = getDispatcherConsumer();
        vtNatsClient.subscribe(consumer);
    }

    private Consumer<Dispatcher> getDispatcherConsumer() {
        MessageHandler messageHandlerForMsg = msg -> {
            String subject = msg.getSubject();
            String msgText = new String(msg.getData(), StandardCharsets.UTF_8);
            LOGGER.info("Received message {}, on subject {}", msgText, subject);
            try {
                notifyService.notifyApplicationMsg(msgText);
            } catch (Exception e) {
                log.error("error,", e);
            }
        };
        MessageHandler messageHandlerForEx = msg -> {
            String subject = msg.getSubject();
            String msgText = new String(msg.getData(), StandardCharsets.UTF_8);
            LOGGER.info("Received message {}, on subject {}", msgText, subject);
            notifyService.notifyApplicationException(vtNatsClient.applicationName, msgText);
        };


        Consumer<Dispatcher> consumer = (dispatcher) -> {
            dispatcher.subscribe(
                NatsConstants.Topic.TOPIC_APPLICATIONMSG,
                messageHandlerForMsg
            );
            log.info("subscribe topic: {}", NatsConstants.Topic.TOPIC_APPLICATIONMSG);

            dispatcher.subscribe(
                NatsConstants.Topic.TOPIC_APPLICATIONEX,
                messageHandlerForEx
            );
            log.info("subscribe topic: {}", NatsConstants.Topic.TOPIC_APPLICATIONEX);

            MessageHandler messageHandlerForTrade = msg -> {
                String msgText = new String(msg.getData(), StandardCharsets.UTF_8);
//                log.info("Received message {}, on subject {}", msgText, subject);

                // {"account_id": "test-0920",
                // "account_type": 2, "order_id": 100,
                // "order_remark": "mid1", "order_status": 56,
                // "order_sysid": "100100", "order_time": 1711692756,
                // "order_type": 23, "order_volume": 12345, "price": 0.579,
                // "price_type": 50, "status_msg": "", "stock_code": "513360.SH",
                // "strategy_name": "FIX", "traded_price": 0.579, "traded_volume": 12345}
                try {
                    IProcessTrade processTrade = CDI.current().select(IProcessTrade.class).get();
                    processTrade.processTrade(msgText);
                } catch (Exception e) {
                    log.error("error when processMsg() {}", msgText, e);
                }

            };
            dispatcher.subscribe(
                NatsConstants.Topic.TOPIC_MODESTEP_TRADE,
                messageHandlerForTrade
            );

        };
        log.info("consumer={}", consumer);
        return consumer;
    }


}
