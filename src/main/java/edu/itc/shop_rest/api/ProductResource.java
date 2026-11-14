package edu.itc.shop_rest.api;

import java.net.URI;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import edu.itc.shop_rest.api.dto.ProductDto;
import edu.itc.shop_rest.api.dto.StockChange;
import edu.itc.shop_rest.domain.Product;
import edu.itc.shop_rest.domain.ProductRepository;
import edu.itc.shop_rest.domain.ProductService;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProductResource {
    private final ProductRepository repo;
    private final ProductService service;

    @Inject
    public ProductResource(ProductRepository repo, ProductService service) {
        this.repo = repo;
        this.service = service;
    }

    @GET
    public List<ProductDto> list() {
        return repo.findAll().stream().map(ProductDto::from).toList();
    }

    @GET
    @Path("{id: \\d+}")
    public ProductDto get(@PathParam("id") long id) {
        return repo.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    @POST
    public Response create(ProductDto dto, @Context UriInfo uriInfo) {
        Product saved = service.create(dto.toProduct(0, 0));
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(saved.id())).build();
        return Response.created(location).entity(ProductDto.from(saved)).build();
    }

    @PUT
    @Path("{id: \\d+}")
    public ProductDto replace(@PathParam("id") long id, ProductDto dto) {
        return ProductDto.from(service.replace(id, dto.toProduct(id, 0)));
    }

    @DELETE
    @Path("{id: \\d+}")
    public Response delete(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    public ProductDto adjustStock(@PathParam("id") long id, StockChange change) {
        return ProductDto.from(service.adjustStock(id, change.delta()));
    }
}