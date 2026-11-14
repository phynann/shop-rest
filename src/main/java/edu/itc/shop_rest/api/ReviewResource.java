package edu.itc.shop_rest.api;

import edu.itc.shop_rest.domain.Review;
import edu.itc.shop_rest.domain.ReviewRepository;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

public class ReviewResource {

    private final long productId;
    private final ReviewRepository repo;

    public ReviewResource(long productId, ReviewRepository repo) {
        this.productId = productId;
        this.repo = repo;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Review> list() {
        return repo.findByProduct(productId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(@Valid Review review, @Context UriInfo uriInfo) {
        Review saved = repo.save(new Review(0, productId, review.stars(), review.author(), review.text()));
        URI location = uriInfo.getRequestUriBuilder().path(Long.toString(saved.id())).build();
        return Response.created(location).build();
    }
}