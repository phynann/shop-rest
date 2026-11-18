package edu.itc.shop_rest.api;

import edu.itc.shop_rest.api.dto.ProductDto;
import edu.itc.shop_rest.domain.Product;
import edu.itc.shop_rest.domain.ProductRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

public class ProductService {

    private final ProductRepository repo;
    private final edu.itc.shop_rest.domain.ProductService mutations;

    @Inject
    public ProductService(ProductRepository repo) {
        this.repo = repo;
        this.mutations = new edu.itc.shop_rest.domain.ProductService(repo);
    }

    public List<Product> search(ProductFilter f) {
        return repo.findAll(); // already sorted by id
    }

    public Product get(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    public Product update(long id, ProductDto dto) {
        return mutations.replace(id, dto.toProduct(id, 0));
    }

    public Product create(ProductDto dto) {
        return mutations.create(dto.toProduct(0, 0));
    }

    public void delete(long id) {
        mutations.delete(id);
    }

    public Product patchStock(long id, int delta) {
        return mutations.adjustStock(id, delta);
    }
}