# Task 2 - CRUD and JSON/XML HTTP transcript

Mutating requests use the development API key (`demo-123`). Product IDs below
are from one run of `shop-rest-all-tests.sh` and may differ on another run.

## Create JSON product (201 + Location)

```sh
curl -i -X POST localhost:8080/api/products \
  -H 'Content-Type: application/json' -H 'X-Api-Key: demo-123' \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
```

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/34
Content-Type: application/json

{"id":34,"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}
```

## Duplicate SKU (409)

```sh
curl -i -X POST localhost:8080/api/products \
  -H 'Content-Type: application/json' -H 'X-Api-Key: demo-123' \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
```

```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json

{"type":"about:blank","title":"Conflict","status":409,"detail":"SKU already exists: TP-01","instance":"/api/products"}
```

## XML round trip (POST, then GET)

```sh
curl -i -X POST localhost:8080/api/products \
  -H 'Content-Type: application/xml' -H 'Accept: application/xml' \
  -H 'X-Api-Key: demo-123' \
  -d '<product><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>'
```

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/35
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8" standalone="yes"?><product id="35"><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>
```

```sh
curl -i localhost:8080/api/products/35 -H 'Accept: application/xml'
```

```http
HTTP/1.1 200 OK
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8" standalone="yes"?><product id="35"><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>
```

## PATCH stock (200; updated representation)

```sh
curl -i -X PATCH localhost:8080/api/products/2/stock \
  -H 'Content-Type: application/json' -H 'X-Api-Key: demo-123' \
  -d '{"delta":-5}'
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

{"id":2,"name":"Mug","sku":"MG-01","price":6.50,"stock":35}
```

## Insufficient stock (409)

```sh
curl -i -X PATCH localhost:8080/api/products/3/stock \
  -H 'Content-Type: application/json' -H 'X-Api-Key: demo-123' \
  -d '{"delta":-1}'
```

```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json

{"type":"about:blank","title":"Conflict","status":409,"detail":"Product 3 has stock 0, cannot apply -1","instance":"/api/products/3/stock"}
```

## DELETE (204, then 404)

```sh
curl -i -X DELETE localhost:8080/api/products/34 -H 'X-Api-Key: demo-123'
```

```http
HTTP/1.1 204 No Content
```

```sh
curl -i -X DELETE localhost:8080/api/products/34 -H 'X-Api-Key: demo-123'
```

```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{"type":"about:blank","title":"Not Found","status":404,"detail":"Product 34 not found","instance":"/api/products/34"}
```

## Unsupported media type (415)

```sh
curl -i -X POST localhost:8080/api/products \
  -H 'Content-Type: text/plain' -H 'X-Api-Key: demo-123' -d 'Teapot'
```

```http
HTTP/1.1 415 Unsupported Media Type
Content-Type: application/problem+json
```
