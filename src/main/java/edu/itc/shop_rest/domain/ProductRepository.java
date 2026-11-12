package edu.itc.shop_rest.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(long id);

    Optional<Product> findBySku(String sku);

    /** Assigns a new id when p.id() == 0; increments the version on every save. */
    Product save(Product p);

    boolean delete(long id);
}