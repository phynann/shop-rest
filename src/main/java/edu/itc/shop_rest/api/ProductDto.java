package edu.itc.shop_rest.api;

import edu.itc.shop_rest.domain.Product;

public record ProductDto(long id, String name, String sku, java.math.BigDecimal price, int stock) {

    public static ProductDto from(Product p) {
        return new ProductDto(p.id(), p.name(), p.sku(), p.price(), p.stock());
    }
}