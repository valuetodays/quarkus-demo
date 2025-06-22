package cn.valuetodays.api2.web.controller;

import java.util.List;
import java.util.Map;

import cn.valuetodays.api2.client.persist.AccessedUrlPersist;
import cn.valuetodays.api2.client.req.PullCookieReq;
import cn.valuetodays.api2.web.asyncevent.Events;
import cn.valuetodays.api2.web.component.CookieCacheComponent;
import cn.vt.rest.third.xueqiu.vo.PushCookieReq;
import cn.vt.util.JsonUtils;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-05-02
 */
@RequestScoped
@Path("/cookie")
@Slf4j
public class CookieController {
    @Inject
    EventBus eventBus;

    @Inject
    CookieCacheComponent cookieCacheComponent;

    @Path("/anon/raw/push")
    @POST
    public Map<String, Object> pushCookies(PushCookieReq req) {
        log.info("pushCookies: {}", req);
        AccessedUrlPersist p = new AccessedUrlPersist();
        p.setUrl(req.getReferer());
        eventBus.publish(Events.EVENT_ACCESSED_URL, JsonUtils.toJson(p));
        String domain = req.getDomain();
        List<String> partitions = req.getCookieTextArr();
        try {
            String cookieArrText = cookieCacheComponent.mergePartitions(partitions);
            cookieCacheComponent.doPut(domain, cookieArrText);
            return Map.of("ok", 1);
        } catch (Exception e) {
            log.error("error when pushCookies", e);
            return Map.of("ok", 0);
        }
    }

    @Path("/anon/pullWeiboCookies")
    @POST
    public Map<String, String> pullWeiboCookies() {
        List<String> excludeNames = List.of("XSRF-TOKEN");
        return cookieCacheComponent.pull(PushCookieReq.DOMAIN_WEIBO, excludeNames);
    }

    @Path("/anon/pullXueqiuCookies")
    @POST
    public Map<String, String> pullXueqiuCookies() {
        return cookieCacheComponent.pullXueqiuCookie();
    }

    @Path("/anon/pull")
    @POST
    public Map<String, String> pullCookie(PullCookieReq req) {
        String domain = req.getDomain();
        return cookieCacheComponent.pull(domain, List.of());
    }


}
