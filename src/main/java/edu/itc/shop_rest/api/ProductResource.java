package edu.itc.shop_rest.api;

import edu.itc.shop_rest.api.dto.ProductDto;
import edu.itc.shop_rest.api.dto.StockChange;
import edu.itc.shop_rest.api.filter.Compress;
import edu.itc.shop_rest.domain.Money;
import edu.itc.shop_rest.domain.Product;
import edu.itc.shop_rest.domain.ProductRepository;
import edu.itc.shop_rest.domain.ReviewRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Compress
public class ProductResource {

    private final ProductService service;
    private final ReviewRepository reviewRepo;

    @Inject
    public ProductResource(ProductService service, ProductRepository repo, ReviewRepository reviewRepo) {
        this.service = service;
        this.reviewRepo = reviewRepo;
    }

    @GET
    public Response list(@BeanParam @Valid ProductFilter f, @Context UriInfo uriInfo) {
        List<Product> all = service.search(f);
        int total = all.size();
        int lastPage = Math.max(0, (total - 1) / f.size);
        long from = (long) f.page * f.size;
        List<ProductDto> page = all.stream().skip(from).limit(f.size)
                .map(ProductDto::from)
                .toList();

        List<Link> links = new ArrayList<>();
        links.add(link(uriInfo, 0, "first"));
        if (f.page > 0) {
            links.add(link(uriInfo, f.page - 1, "prev"));
        }
        if (f.page < lastPage) {
            links.add(link(uriInfo, f.page + 1, "next"));
        }
        links.add(link(uriInfo, lastPage, "last"));

        return Response.ok(new GenericEntity<List<ProductDto>>(page) {})
                .links(links.toArray(new Link[0]))
                .header("X-Total-Count", total)
                .build();
    }

    private static Link link(UriInfo uriInfo, int page, String rel) {
        return Link.fromUriBuilder(uriInfo.getRequestUriBuilder().replaceQueryParam("page", page))
                .rel(rel).build();
    }

    @POST
    public Response create(@Valid ProductDto dto, @Context UriInfo uriInfo) {
        Product saved = service.create(dto);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(saved.id()))
                .build();
        return Response.created(location).entity(ProductDto.from(saved)).build();
    }

    @GET
    @Path("{id: \\d+}")
    @Produces({"application/json; qs=1.0", "application/vnd.shop.v1+json", MediaType.APPLICATION_XML})
    public Response get(@PathParam("id") long id, @Context Request request) {
        Product p = service.get(id);
        EntityTag tag = new EntityTag(Integer.toHexString(p.version()));
        Response.ResponseBuilder cond = request.evaluatePreconditions(tag);
        if (cond != null) {
            return cond.tag(tag).build();
        }
        CacheControl cc = new CacheControl();
        cc.setPrivate(true);
        cc.setMaxAge(60);
        cc.setNoTransform(true);
        return Response.ok(ProductDto.from(p)).tag(tag).cacheControl(cc).build();
    }

    @GET
    @Path("{id: \\d+}")
    @Produces("application/vnd.shop.v2+json")
    public Response getV2(@PathParam("id") long id) {
        Product p = service.get(id);
        ProductV2 v2 = new ProductV2(p.id(), p.name(), p.sku(),
                new Money(p.price(), "EUR"), p.stock(), p.stock() > 0);
        return Response.ok(v2).build();
    }

    @PUT
    @Path("{id: \\d+}")
    public Response put(@PathParam("id") long id, @Valid ProductDto dto) {
        Product updated = service.update(id, dto);
        EntityTag newTag = new EntityTag(Integer.toHexString(updated.version()));
        CacheControl cc = new CacheControl();
        cc.setPrivate(true);
        cc.setMaxAge(60);
        cc.setNoTransform(true);
        return Response.ok(ProductDto.from(updated)).tag(newTag).cacheControl(cc).build();
    }

    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response patchStock(@PathParam("id") long id, @Valid StockChange change) {
        Product updated = service.patchStock(id, change.delta());
        EntityTag tag = new EntityTag(Integer.toHexString(updated.version()));
        CacheControl cc = new CacheControl();
        cc.setPrivate(true);
        cc.setMaxAge(60);
        cc.setNoTransform(true);
        return Response.ok(ProductDto.from(updated)).tag(tag).cacheControl(cc).build();
    }

    @DELETE
    @Path("{id: \\d+}")
    public Response delete(@PathParam("id") long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @Path("{id: \\d+}/reviews")
    public ReviewResource reviews(@PathParam("id") long id) {
        service.get(id); // 404 when the product is unknown
        return new ReviewResource(id, reviewRepo);
    }

}