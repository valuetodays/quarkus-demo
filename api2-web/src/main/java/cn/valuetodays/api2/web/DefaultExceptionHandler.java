package cn.valuetodays.api2.web;

import cn.valuetodays.api2.basic.component.VtNatsClient;
import cn.vt.R;
import cn.vt.exception.CommonException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.List;

@Provider
@Slf4j
public class DefaultExceptionHandler implements ExceptionMapper<Exception> {
    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    VtNatsClient natsClient;
    private List<String> excludeMsgsPrefixNotNotify = List.of("No static resource");

    @Override
    public Response toResponse(Exception exception) {
        String msg;
        if (exception instanceof CommonException) {
            msg = exception.getMessage();
        } else {
            msg = "Exception: " + exception.getMessage();
        }
        log.error("error", exception);

        boolean excludeByMsgPrefix = excludeMsgsPrefixNotNotify.stream().anyMatch(msg::startsWith);
        if (!excludeByMsgPrefix) {
            managedExecutor.execute(() -> {
                natsClient.publishApplicationException(msg, exception);
            });
        }
        return Response.status(Response.Status.OK).entity(R.fail(msg)).build();
    }
}
