package edu.itc.shop_rest;

import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import edu.itc.shop_rest.domain.InMemoryProductRepository;
import edu.itc.shop_rest.domain.ProductRepository;
import edu.itc.shop_rest.domain.ProductService;
import edu.itc.shop_rest.api.ProductResource;
import edu.itc.shop_rest.api.error.ProblemMapper;
import edu.itc.shop_rest.api.error.ValidationMapper;
import edu.itc.shop_rest.api.filter.ApiKeyFilter;
import edu.itc.shop_rest.api.filter.CorrelationFilter;
import edu.itc.shop_rest.api.filter.CorsFilter;
import edu.itc.shop_rest.api.filter.GzipWriterInterceptor;
import edu.itc.shop_rest.api.params.MoneyConverterProvider;

public class Main {
    public static final URI BASE = URI.create("http://localhost:8080/api/");
    public static ResourceConfig config() {
        var repo = new InMemoryProductRepository();
        var service = new ProductService(repo);
        return new ResourceConfig()
        .register(ProductResource.class)
        .register(MoneyConverterProvider.class)
        .register(ValidationMapper.class)
        .register(ProblemMapper.class)
        .register(CorrelationFilter.class)
        .register(CorsFilter.class)
        .register(ApiKeyFilter.class)
        .register(GzipWriterInterceptor.class)
        .register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(repo).to(ProductRepository.class);
                bind(service).to(ProductService.class);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE, config());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        System.out.println("shop-rest listening on " + BASE);
        Thread.currentThread().join();
    }
}