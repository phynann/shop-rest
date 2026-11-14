package edu.itc.shop_rest;

import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import edu.itc.shop_rest.domain.InMemoryProductRepository;
import edu.itc.shop_rest.domain.ProductRepository;
import edu.itc.shop_rest.domain.ProductService;

public class Main {
    public static final URI BASE = URI.create("http://localhost:8080/api/");
    public static ResourceConfig config() {
        var repo = new InMemoryProductRepository();
        var service = new ProductService(repo);
        return new ResourceConfig()
                .packages("edu.itc.shop_rest.api")
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