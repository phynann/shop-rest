# Task 5 — HTTP interaction notes

Base URL: `http://localhost:8080/api/`

## 1. Paged list — page 1, size 10

```http
GET /api/products?page=1&size=10
```

```
HTTP/1.1 200 OK
Link: <http://localhost:8080/api/products?size=10&page=0>; rel="first",
      <http://localhost:8080/api/products?size=10&page=0>; rel="prev",
      <http://localhost:8080/api/products?size=10&page=2>; rel="next",
      <http://localhost:8080/api/products?size=10&page=3>; rel="last"
X-Total-Count: 33
Content-Type: application/json
```

Body: products 11-20 (10 items). `page`/`size` are validated (`@Min(0)`, `@Min(1) @Max(100)`);
out-of-range values answer 400. `X-Total-Count` reflects the total after filtering
(33 products seeded) and every `Link` relation is built from the request URI via
`UriInfo.getRequestUriBuilder().replaceQueryParam("page", n)`. `prev` is omitted on
the first page, `next` on the last.

## 2. GET a product — ETag and Cache-Control

```http
GET /api/products/1
```

```
HTTP/1.1 200 OK
ETag: "1"
Cache-Control: private, no-transform, max-age=60

{"id":1,"name":"Kettle XL","sku":"KT-01","price":24.90,"stock":12}
```

## 3. Conditional GET — 304 Not Modified

```http
GET /api/products/1
If-None-Match: "1"
```

```
HTTP/1.1 304 Not Modified
ETag: "1"
```

`request.evaluatePreconditions(tag)` returns a non-null builder when the
`If-None-Match` tag matches the current version, so a 304 (carrying the same tag)
is returned without a body.

## 4. PUT without If-Match — 428 Precondition Required

```http
PUT /api/products/1
X-Api-Key: demo-123
Content-Type: application/json

{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}
```

```
HTTP/1.1 428 Precondition Required
Content-Type: application/json

{"status":428,"title":"Precondition Required","detail":"If-Match header is required"}
```

## 5. PUT with a stale tag — 412 Precondition Failed

Current version of product 1 is 1 (ETag `"1"`):

```http
PUT /api/products/1
X-Api-Key: demo-123
If-Match: "0"
Content-Type: application/json

{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}
```

```
HTTP/1.1 412 Precondition Failed
ETag: "1"
```

## 6. PUT with the right tag — 200 + new ETag

```http
PUT /api/products/1
X-Api-Key: demo-123
If-Match: "1"
Content-Type: application/json

{"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}
```

```
HTTP/1.1 200 OK
ETag: "2"
Cache-Control: private, no-transform, max-age=60

{"id":1,"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}
```

The save increments the version (1 → 2), so the new ETag is `"2"`.
(`PATCH /api/products/{id}/stock` keeps working without any preconditions.)

## 7. Versioning by media type — v1 vs v2 of the same product

Plain clients keep v1:

```http
GET /api/products/1
Accept: application/json
```

```
HTTP/1.1 200 OK
Content-Type: application/json

{"id":1,"name":"Kettle XL","sku":"KT-01","price":29.90,"stock":12}
```

Asking for the v2 media type returns the v2 representation:

```http
GET /api/products/1
Accept: application/vnd.shop.v2+json
```

```
HTTP/1.1 200 OK
Content-Type: application/json

{"id":1,"name":"Kettle XL","sku":"KT-01","price":{"amount":29.90,"currency":"EUR"},
 "stock":12,"available":true}
```

The existing endpoint is marked `application/json; qs=1.0` and
`application/vnd.shop.v1+json`; a second endpoint with
`@Produces("application/vnd.shop.v2+json")` returns `ProductV2` (`Money` price,
`available` flag).

## 8. Review sub-resource — POST then GET

```http
POST /api/products/1/reviews
X-Api-Key: demo-123
Content-Type: application/json

{"stars":5,"author":"Ana","text":"Boils fast."}
```

```
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/1/reviews/1
```

The body is validated (`@Min(1) @Max(5) stars`, `@NotBlank author`,
`@Size(max = 500) text`; violations answer 400).

```http
GET /api/products/1/reviews
```

```
HTTP/1.1 200 OK
Content-Type: application/json

[{"id":1,"productId":1,"stars":5,"author":"Ana","text":"Boils fast."}]
```

The locator `@Path("{id}/reviews")` returns a new `ReviewResource`; it looks the
product up first, so an unknown product 404s before the sub-resource is involved:

```http
GET /api/products/999/reviews
```

```
HTTP/1.1 404 Not Found
```