package edu.itc.shop_rest.domain;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    List<Review> findByProduct(long productId);

    Optional<Review> findById(long id);

    /** Assigns a new id when r.id() == 0. */
    Review save(Review r);
}