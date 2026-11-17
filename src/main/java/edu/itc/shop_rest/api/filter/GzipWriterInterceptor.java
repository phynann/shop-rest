package edu.itc.shop_rest.api.filter;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

@Provider
@Compress
public class GzipWriterInterceptor implements WriterInterceptor {
    @Context
    HttpHeaders requestHeaders;

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException {
        String enc = requestHeaders != null ? requestHeaders.getHeaderString(HttpHeaders.ACCEPT_ENCODING) : null;
        if (enc != null && enc.contains("gzip")) {
            ctx.getHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, "gzip");
            ctx.getHeaders().putSingle(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
            GZIPOutputStream gzipOut = new GZIPOutputStream(ctx.getOutputStream());
            ctx.setOutputStream(gzipOut);
            try {
                ctx.proceed();
            } finally {
                gzipOut.finish();
            }
            return;
        }
        ctx.proceed();
    }
}
