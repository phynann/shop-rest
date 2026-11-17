package edu.itc.shop_rest.api;

import edu.itc.shop_rest.domain.Product;
import edu.itc.shop_rest.domain.ProductRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

public class ProductService {

    private final ProductRepository repo;

    @Inject
    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> search(ProductFilter f) {
        return repo.findAll(); // already sorted by id
    }

    public Product get(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    public Product update(long id, ProductDto dto) {
        Product current = get(id);
        Product updated = new Product(id, dto.name(), dto.sku(), dto.price(), dto.stock(), current.version());
        return repo.save(updated);
    }

    public Product patchStock(long id, int stock) {
        Product current = get(id);
        Product updated = new Product(id, current.name(), current.sku(), current.price(), stock, current.version());
        return repo.save(updated);
    }
}