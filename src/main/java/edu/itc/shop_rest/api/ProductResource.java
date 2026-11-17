package edu.itc.shop_rest.api;

import edu.itc.shop_rest.domain.Money;
import edu.itc.shop_rest.domain.Product;
import edu.itc.shop_rest.domain.ProductRepository;
import edu.itc.shop_rest.domain.ReviewRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

@Path("/products")
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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("id") long id,
                        @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
                        @Valid ProductDto dto) {
        Product current = service.get(id);
        EntityTag tag = new EntityTag(Integer.toHexString(current.version()));

        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_REQUIRED)
                    .entity(new Problem(Response.Status.PRECONDITION_REQUIRED.getStatusCode(),
                            "Precondition Required", "If-Match header is required"))
                    .build();
        }
        String requested = ifMatch.trim();
        String currentTag = tag.toString(); // includes surrounding quotes, e.g. "1"
        if (!"*".equals(requested) && !currentTag.equals(requested)
                && !currentTag.equals("\"" + requested + "\"")) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(new Problem(Response.Status.PRECONDITION_FAILED.getStatusCode(),
                            "Precondition Failed", "If-Match does not match current version"))
                    .tag(tag).build();
        }

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
    public Response patchStock(@PathParam("id") long id, @Valid StockPatch patch) {
        Product updated = service.patchStock(id, patch.stock());
        EntityTag tag = new EntityTag(Integer.toHexString(updated.version()));
        CacheControl cc = new CacheControl();
        cc.setPrivate(true);
        cc.setMaxAge(60);
        cc.setNoTransform(true);
        return Response.ok(ProductDto.from(updated)).tag(tag).cacheControl(cc).build();
    }

    @Path("{id: \\d+}/reviews")
    public ReviewResource reviews(@PathParam("id") long id) {
        service.get(id); // 404 when the product is unknown
        return new ReviewResource(id, reviewRepo);
    }

    private record Problem(int status, String title, String detail) {
    }

    public record StockPatch(@jakarta.validation.constraints.Min(0) int stock) {
    }
}