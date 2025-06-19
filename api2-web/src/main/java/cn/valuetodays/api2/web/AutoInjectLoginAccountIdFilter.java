package cn.valuetodays.api2.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;

import cn.valuetodays.quarkus.commons.base.BaseAuthorizationController;
import cn.vt.web.req.BaseAccountableReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@Provider
//@PreMatching
//@Priority(1)
@Slf4j
public class AutoInjectLoginAccountIdFilter extends BaseAuthorizationController implements ContainerRequestFilter {

    @Inject
    ObjectMapper objectMapper;
    @Inject
    ResourceInfo resourceInfo; // 注入当前请求信息

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 只拦截 POST，跳过 GET/DELETE
        if (!"POST".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }
        Method resourceMethod = resourceInfo.getResourceMethod();
        if (Objects.isNull(resourceMethod)) {
            return;
        }
        Parameter[] parameters = resourceMethod.getParameters();
        if (ArrayUtils.isEmpty(parameters)) {
            return;
        }
        boolean isSubClassOfBaseAccountableReq = false;
        for (Parameter parameter : parameters) {
            Class<?> type = parameter.getType();
            if (BaseAccountableReq.class.isAssignableFrom(type)) {
                isSubClassOfBaseAccountableReq = true;
                break;
            }
        }
        if (!isSubClassOfBaseAccountableReq) {
            return;
        }
        // 读取原始请求体
        InputStream originalStream = requestContext.getEntityStream();
        byte[] requestBodyBytes = originalStream.readAllBytes();
        // 这里可以选择性解析，如果解析失败就跳过
        try {
            // 简化：这里只做统一赋值，强制转为 Map 处理
            var map = objectMapper.readValue(requestBodyBytes, Map.class);
            map.put("accountId", getCurrentAccountId());
            byte[] modifiedBody = objectMapper.writeValueAsBytes(map);
            requestContext.setEntityStream(new ByteArrayInputStream(modifiedBody));
            return;
        } catch (Exception ignored) {
            log.warn("warn when", ignored);
        }
        // 恢复请求体，防止请求体丢失
        requestContext.setEntityStream(new ByteArrayInputStream(requestBodyBytes));
    }

}
