package edu.itc.shop_rest;

import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import edu.itc.shop_rest.api.ProductService;
import edu.itc.shop_rest.domain.InMemoryProductRepository;
import edu.itc.shop_rest.domain.InMemoryReviewRepository;
import edu.itc.shop_rest.domain.ProductRepository;
import edu.itc.shop_rest.domain.ReviewRepository;
import org.glassfish.jersey.server.validation.ValidationFeature;

public class Main {
    public static final URI BASE = URI.create("http://localhost:8080/api/");

    /** The application: scanned resources/providers + shared repositories. */
    public static ResourceConfig config() {
        return new ResourceConfig()
                .packages("edu.itc.shop_rest.api")
                .register(ValidationFeature.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(new InMemoryProductRepository()).to(ProductRepository.class);
                        bind(new InMemoryReviewRepository()).to(ReviewRepository.class);
                        bind(ProductService.class).to(ProductService.class);
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