# Task 4 - HTTP transcript

### 1. POST without API key (401 problem+json)
```
$ curl -i -X POST localhost:8080/api/products -H "Content-Type: application/json" -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 401 Unauthorized
WWW-Authenticate: ApiKey realm="shop"
X-Correlation-Id: 3c2c4a98-d74d-443a-8344-8fed0a852bf8
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
Content-Type: application/problem+json
Content-Length: 125

{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Missing or invalid X-Api-Key","instance":"/api/products"}
```

### 2. POST with wrong API key (401)
```
$ curl -i -X POST localhost:8080/api/products -H "Content-Type: application/json" -H "X-Api-Key: wrong-key" -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 401 Unauthorized
WWW-Authenticate: ApiKey realm="shop"
X-Correlation-Id: 5d15aa72-82f6-4181-8ddf-12d84a1c5105
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
Content-Type: application/problem+json
Content-Length: 125

{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Missing or invalid X-Api-Key","instance":"/api/products"}
```

### 3. POST with right API key (201)
```
$ curl -i -X POST localhost:8080/api/products -H "Content-Type: application/json" -H "X-Api-Key: demo-123" -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/4
X-Correlation-Id: 8a0bf9dd-aac0-4133-b0c8-8f3b7d9902ba
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
Content-Type: application/json
Content-Length: 62

{"id":4,"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}
```

### 4. OPTIONS preflight
```
$ curl -i -X OPTIONS localhost:8080/api/products -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: POST"
HTTP/1.1 200 OK
X-Correlation-Id: 800fc56e-06ba-44cd-b77f-714fc8b42b1a
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
Content-Length: 0
```

### 5. GET showing X-Correlation-Id echoed back
```
$ curl -i localhost:8080/api/products/1 -H "X-Correlation-Id: order-check-42"
HTTP/1.1 200 OK
X-Correlation-Id: order-check-42
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
Content-Type: application/json
Content-Length: 63

{"id":1,"name":"Kettle","sku":"KT-01","price":24.90,"stock":12}
```

### 6. curl --compressed -v showing Content-Encoding: gzip
```
$ curl -v --compressed localhost:8080/api/products
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
*   Trying 127.0.0.1:8080...
* Established connection to localhost (127.0.0.1 port 8080)
> GET /api/products HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.21.0
> Accept: */*
> Accept-Encoding: deflate, gzip
> 
< HTTP/1.1 200 OK
< X-Correlation-Id: ba964745-979c-4be8-999c-37c55a660716
< Access-Control-Allow-Origin: http://localhost:5173
< Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
< Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
< Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
< Content-Encoding: gzip
< Vary: Accept-Encoding
< Content-Type: application/json
< Content-Length: 140
< 
[{"id":1,"name":"Kettle","sku":"KT-01","price":24.90,"stock":12},{"id":2,"name":"Mug","sku":"MG-01","price":6.50,"stock":40},{"id":3,"name":"Tea","sku":"TE-01","price":4.20,"stock":0},{"id":4,"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}]
```

### 7. Same GET without Accept-Encoding showing no compression
```
$ curl -i localhost:8080/api/products
HTTP/1.1 200 OK
X-Correlation-Id: e69e6862-d812-4180-93c5-538d0dcdf09d
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE
Access-Control-Allow-Headers: Content-Type,X-Api-Key,X-Correlation-Id,If-Match
Access-Control-Expose-Headers: Location,ETag,Link,X-Total-Count,X-Correlation-Id
Content-Type: application/json
Content-Length: 247

[{"id":1,"name":"Kettle","sku":"KT-01","price":24.90,"stock":12},{"id":2,"name":"Mug","sku":"MG-01","price":6.50,"stock":40},{"id":3,"name":"Tea","sku":"TE-01","price":4.20,"stock":0},{"id":4,"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}]
```

### Server Log Output
```
INFO: POST /api/products -> 401 [3c2c4a98-d74d-443a-8344-8fed0a852bf8] 9 ms
INFO: POST /api/products -> 401 [5d15aa72-82f6-4181-8ddf-12d84a1c5105] 0 ms
INFO: POST /api/products -> 201 [8a0bf9dd-aac0-4133-b0c8-8f3b7d9902ba] 117 ms
INFO: OPTIONS /api/products -> 200 [800fc56e-06ba-44cd-b77f-714fc8b42b1a] 0 ms
INFO: GET  /api/products/1 -> 200 [order-check-42] 14 ms
INFO: GET  /api/products -> 200 [ba964745-979c-4be8-999c-37c55a660716] 10 ms
INFO: GET  /api/products -> 200 [e69e6862-d812-4180-93c5-538d0dcdf09d] 1 ms
```
