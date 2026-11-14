package edu.itc.shop_rest.domain;

import jakarta.ws.rs.NotFoundException;

public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public synchronized Product create(Product p) {
        repo.findBySku(p.sku()).ifPresent(x -> { throw new DuplicateSkuException(p.sku()); });
        return repo.save(new Product(0, p.name(), p.sku(), p.price(), p.stock(), 0));
    }

    public synchronized Product replace(long id, Product p) {
        Product existing = get(id);
        repo.findBySku(p.sku()).ifPresent(other -> {
            if (other.id() != id) throw new DuplicateSkuException(p.sku());
        });
        return repo.save(new Product(id, p.name(), p.sku(), p.price(), p.stock(), existing.version()));
    }

    public synchronized void delete(long id) {
        if (!repo.delete(id)) throw notFound(id);
    }

    public synchronized Product adjustStock(long id, int delta) {
        Product p = get(id);
        int newStock = p.stock() + delta;
        if (newStock < 0) throw new InsufficientStockException(id, p.stock(), delta);
        return repo.save(new Product(id, p.name(), p.sku(), p.price(), newStock, p.version()));
    }

    private Product get(long id) {
        return repo.findById(id).orElseThrow(() -> notFound(id));
    }

    private static NotFoundException notFound(long id) {
        return new NotFoundException("Product " + id + " not found");
    }
}