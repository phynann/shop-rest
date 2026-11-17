package edu.itc.shop_rest.api;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import edu.itc.shop_rest.api.dto.ProductDto;
import edu.itc.shop_rest.api.dto.StockChange;
import edu.itc.shop_rest.api.filter.Compress;
import edu.itc.shop_rest.api.params.ProductFilter;
import edu.itc.shop_rest.domain.Money;
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
    @Compress
    public List<ProductDto> list(@BeanParam @Valid ProductFilter f) {
        requireEur(f.minPrice, "minPrice");
        requireEur(f.maxPrice, "maxPrice");

        Comparator<Product> cmp = switch (f.sortField()) {
            case "name"  -> Comparator.comparing((Product p) -> p.name().toLowerCase());
            case "price" -> Comparator.comparing(Product::price);
            case "stock" -> Comparator.comparingInt(Product::stock);
            default -> throw new BadRequestException(
                    "Unknown sort field '" + f.sortField() + "'; use name, price or stock");
        };
        cmp = cmp.thenComparingLong(Product::id);
        if (f.descending()) cmp = cmp.reversed();

        return repo.findAll().stream()
                .filter(p -> f.q == null || p.name().toLowerCase().contains(f.q.toLowerCase()))
                .filter(p -> f.minPrice == null || p.price().compareTo(f.minPrice.amount()) >= 0)
                .filter(p -> f.maxPrice == null || p.price().compareTo(f.maxPrice.amount()) <= 0)
                .filter(p -> !f.inStock || p.stock() > 0)
                .sorted(cmp)
                .map(ProductDto::from)
                .toList();
    }

    private static void requireEur(Money m, String param) {
        if (m != null && !"EUR".equals(m.currency()))
            throw new BadRequestException(param + " must be in EUR");
    }

    @GET
    @Path("{id: \\d+}")
    public ProductDto get(@PathParam("id") long id) {
        return repo.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    @POST
    public Response create(@Valid @NotNull ProductDto dto, @Context UriInfo uriInfo) {
        Product saved = service.create(dto.toProduct(0, 0));
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(saved.id())).build();
        return Response.created(location).entity(ProductDto.from(saved)).build();
    }

    @PUT
    @Path("{id: \\d+}")
    public ProductDto replace(@PathParam("id") long id, @Valid @NotNull ProductDto dto) {
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
    public ProductDto adjustStock(@PathParam("id") long id, @Valid @NotNull StockChange change) {
        return ProductDto.from(service.adjustStock(id, change.delta()));
    }
}