package edu.itc.shop_rest.api.filter;

import java.util.UUID;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class CorrelationFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final System.Logger LOGGER = System.getLogger(CorrelationFilter.class.getName());
    private static final String PROP_CORRELATION_ID = "correlationId";
    private static final String PROP_START_TIME = "startTime";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String correlationId = (String) requestContext.getProperty(PROP_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = requestContext.getHeaderString(HEADER_CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            requestContext.setProperty(PROP_CORRELATION_ID, correlationId);
        }
        if (requestContext.getProperty(PROP_START_TIME) == null) {
            requestContext.setProperty(PROP_START_TIME, System.currentTimeMillis());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String correlationId = (String) requestContext.getProperty(PROP_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = requestContext.getHeaderString(HEADER_CORRELATION_ID);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
        }
        responseContext.getHeaders().putSingle(HEADER_CORRELATION_ID, correlationId);

        Long startTime = (Long) requestContext.getProperty(PROP_START_TIME);
        long duration = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0;
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getRequestUri().getPath();
        int status = responseContext.getStatus();

        LOGGER.log(System.Logger.Level.INFO, String.format("%-4s %s -> %d [%s] %d ms", method, path, status, correlationId, duration));
    }
}
