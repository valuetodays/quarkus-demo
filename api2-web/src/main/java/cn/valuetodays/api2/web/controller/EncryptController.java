package cn.valuetodays.api2.web.controller;

import java.util.Map;

import cn.valuetodays.api2.web.component.CookieCacheComponent;
import cn.vt.R;
import cn.vt.util.RSAUtils;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * .
 *
 * @author lei.liu
 * @since 2024-09-19
 */
@RequestScoped
@Path("/encrypt")
public class EncryptController {
    @Inject
    RedisDataSource connection;

    @Path("/anon/getPublicKey")
    @POST
    public R<String> getPublicKey() {
        final HashCommands<String, String, String> hashCommands = connection.hash(String.class);
        Map<String, String> hmgetted = hashCommands.hmget(CookieCacheComponent.cacheKey,
            CookieCacheComponent.fieldPubKey, CookieCacheComponent.fieldPriKey);
        if (MapUtils.isNotEmpty(hmgetted)) {
            String pri = hmgetted.get(CookieCacheComponent.fieldPriKey);
            String pub = hmgetted.get(CookieCacheComponent.fieldPubKey);
            if (StringUtils.isNoneBlank(pri, pub)) {
                return R.success(pub);
            }
        }
        String[] pair = RSAUtils.genKeyPair();
        String pub = pair[0];
        String pri = pair[1];
        Map<String, String> hashToPut = Map.of(CookieCacheComponent.fieldPubKey, pub, CookieCacheComponent.fieldPriKey, pri);
        hashCommands.hset(CookieCacheComponent.cacheKey, hashToPut);
        return R.success(pub);
    }
}
