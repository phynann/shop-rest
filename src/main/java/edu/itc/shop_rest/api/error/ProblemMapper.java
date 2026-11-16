package edu.itc.shop_rest.api.error;

import java.util.UUID;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import edu.itc.shop_rest.api.dto.Problem;

@Provider
public class ProblemMapper implements ExceptionMapper<Throwable> {

    private static final System.Logger LOG = System.getLogger("api");

    @Context UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        String path = uriInfo.getRequestUri().getPath();
        Problem problem;

        if (ex instanceof WebApplicationException wae) {
            int status = wae.getResponse().getStatus();
            var known = Response.Status.fromStatusCode(status);
            String title = known != null ? known.getReasonPhrase() : "Error";
            problem = new Problem("about:blank", title, status, wae.getMessage(), path, null);
        } else {
            String ref = UUID.randomUUID().toString();
            LOG.log(System.Logger.Level.ERROR, "Unhandled error, reference " + ref, ex);
            problem = new Problem("about:blank", "Internal Server Error", 500,
                                  "reference " + ref, path, null);
        }
        return Response.status(problem.status()).type(Problem.MEDIA_TYPE).entity(problem).build();
    }
}