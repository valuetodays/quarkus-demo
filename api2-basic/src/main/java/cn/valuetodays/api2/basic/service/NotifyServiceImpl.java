package cn.valuetodays.api2.basic.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.valuetodays.api2.basic.enums.NotifyEnums;
import cn.valuetodays.api2.basic.vo.BarkDict;
import cn.valuetodays.api2.basic.vo.PushVocechatTextReq;
import cn.valuetodays.api2.web.common.NotifyService;
import cn.vt.util.HttpClient4Utils;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2023-09-25
 */
@ApplicationScoped
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    private static final String DICT_TYPE_CACHE_KEY = "cache.dictType";
    @Inject
    DictTypeService dictTypeService;
    @Inject
    RedisDataSource stringRedisTemplate;
    @Inject
    VocechatServiceImpl vocechatService;

    public void notify(String title, String content, String group, boolean withSound) {
        ValueCommands<String, BarkDict> barkDictValueCommands = stringRedisTemplate.value(BarkDict.class);
        BarkDict cached = barkDictValueCommands.get(DICT_TYPE_CACHE_KEY);
        if (Objects.isNull(cached)) {
            cached = dictTypeService.getBarkDict();
            barkDictValueCommands.setex(DICT_TYPE_CACHE_KEY, Duration.ofMinutes(10).getSeconds(), cached);
        }
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("device_key", cached.getDeviceId());
        requestMap.put("title", title);
        requestMap.put("body", content);
        requestMap.put("group", group);
        if (withSound) {
            requestMap.put("sound", "newmail");
        }
        try {
            HttpClient4Utils.doPostJson(cached.getUrl(), requestMap, null);
        } catch (Exception e) {
            log.error("error when #notify()", e);
        }
        try {
            PushVocechatTextReq vocechatTextReq = new PushVocechatTextReq();
            vocechatTextReq.setContent(content);
            vocechatTextReq.setPlainText(true);
            vocechatTextReq.useToGroupId(2);
            vocechatService.pushVocechatText(vocechatTextReq);
        } catch (Exception e) {
            log.error("vocechatService.pushVocechatText", e);
        }
    }

    public void notify(String title, String content, String group) {
        this.notify(title, content, group, true);
    }

    public void notifyCdci(String title, String content) {
        this.notify(title, content, NotifyEnums.Group.CD_CI.getTitle());
    }

    /**
     * 推送送并锁定1天，使1天内不能出来同样的推送
     */
//    @DistributeLock(id = "notifyWithLock12Hours--#{#title}", milliSeconds = TimeConstants.T1d / 2)
    public void notifyWithLock12Hours(String title, String content, String group) {
        this.notify(title, content, group);
    }

    @Override
    public void notifyAShareDailyTurnAmount(int yyyyMMdd, String content) {
        this.notify(
            "【资本市场】A股今日" + yyyyMMdd + "成交额",
            content,
            NotifyEnums.Group.CAPITAL.getTitle(),
            true
        );
    }

    @Override
    public void notifyStockMinutePrice() {
        String content = "股票分时价格数据更新完毕";
        this.notify(
            "【资本市场】" + content,
            content,
            NotifyEnums.Group.CAPITAL.getTitle(),
            true
        );
    }

    public void notifyXueQiuImages(int n) {
        NotifyEnums.Group group = NotifyEnums.Group.XUEQIU_IMAGE;
        this.notify(
            "[" + group.name() + "]UPDATED",
            "updated " + n + " image(s)",
            group.getTitle(),
            true
        );
    }

    @Override
    public void notifyNoCookie(String domain) {
        NotifyEnums.Group apiAlarm = NotifyEnums.Group.API_ALARM;
        final String title = "[" + apiAlarm.name() + "]no cookie: " + domain;
        final String content = "can not read cookie from " + domain + ", please fix it.";

        ValueCommands<String, String> valueCommands = stringRedisTemplate.value(String.class);
        final String key = "notify" + domain;
        // 避免瞬间多次推送
        boolean flag = valueCommands.setnx(key, "1");
        if (flag) {
            KeyCommands<String> keyCommands = stringRedisTemplate.key(String.class);
            keyCommands.expire(key, Duration.ofMinutes(10));
            this.notify(
                title,
                content,
                apiAlarm.getTitle(),
                true
            );
        }
    }

    public void notifyWarn(String msg) {
        this.notify(
            "WARN",
            msg,
            NotifyEnums.Group.WARN.getTitle(),
            true
        );
    }

    public void notifyWrongHedgedData(String msg) {
        NotifyEnums.Group group = NotifyEnums.Group.CAPITAL;
        this.notify(
            "[" + group.name() + "] hedged stock trades maybe wrong, please check. ",
            "info: " + msg,
            group.getTitle(),
            true
        );
    }

    public void notifyApplicationException(String application, String msg) {
        NotifyEnums.Group group = NotifyEnums.Group.APPLICATION_EXCEPTION;
        this.notify(
            application + " EXCEPTION!",
            msg,
            group.getTitle(),
            true
        );
    }

    public void notifyApplicationMsg(String msg) {
        NotifyEnums.Group group = NotifyEnums.Group.APPLICATION_MSG;
        this.notify(
            "Application Msg",
            msg,
            group.getTitle(),
            true
        );
    }
}
