$ curl -i localhost:8080/api/products
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 220

[{"id":1,"name":"Kettle","sku":"KT-01","price":24.90,"stock":12,"version":1},{"id":2,"name":"Mug","sku":"MG-01","price":6.50,"stock":40,"version":1},{"id":3,"name":"Tea","sku":"TE-01","price":4.20,"stock":0,"version":1}]

$ curl -i localhost:8080/api/products/2
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 71

{"id":2,"name":"Mug","sku":"MG-01","price":6.50,"stock":40,"version":1}

$ curl -i localhost:8080/api/products/99
HTTP/1.1 404 Not Found
Content-Length: 0



$ curl -i localhost:8080/api/products/abc
HTTP/1.1 404 Not Found
Content-Length: 0



