Task 3 - HTTP transcript

Parameters and validation: @BeanParam filtering, ParamConverter for Money, Bean Validation, and RFC 9457 application/problem+json errors.

1. Filtered and sorted list
$ curl -i 'localhost:8080/api/products?minPrice=5%20EUR&inStock=true&sort=price,desc'
<PASTE REAL OUTPUT: expect 200, Kettle (24.90) then Mug (6.50)>
2. minPrice=abc (400 from the converter)
$ curl -i 'localhost:8080/api/products?minPrice=abc'
<PASTE REAL OUTPUT: expect 400, application/problem+json, detail "Cannot parse Money from 'abc'">
3. sort=colour,asc (400)
$ curl -i 'localhost:8080/api/products?sort=colour,asc'
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json
Connection: close
Content-Length: 149

{"type":"about:blank","title":"Bad Request","status":400,"detail":"Unknown sort field 'colour'; use name, price or stock","instance":"/api/products"}
4. Invalid POST with three field errors
$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"name":"","sku":"kettle","price":0,"stock":1}'
<PASTE REAL OUTPUT: expect 400, "Validation failed", errors for name, sku and price>
5. 404 as problem+json
$ curl -i localhost:8080/api/products/99
HTTP/1.1 404 Not Found
Content-Type: application/problem+json
Content-Length: 117

{"type":"about:blank","title":"Not Found","status":404,"detail":"Product 99 not found","instance":"/api/products/99"}
6. The 500 experiment

Temporarily threw IllegalStateException("boom") as the first line of ProductResource.get(...).

$ curl -i localhost:8080/api/products/1
<PASTE REAL OUTPUT: expect 500, application/problem+json, detail "reference <uuid>", no stack trace>

The stack trace and the same reference id appeared only in the server console, not in the response body. The throw was removed afterwards.