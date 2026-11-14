# Task 2 - HTTP transcript
```
$ curl -i -X POST localhost:8080/api/products -H Content-Type: application/json -d {"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/4
Content-Type: application/json
Content-Length: 62

{"id":4,"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}
$ curl -i -X POST localhost:8080/api/products -H Content-Type: application/json -d {"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}
HTTP/1.1 409 Conflict
Content-Length: 0


$ curl -i localhost:8080/api/products/4 -H Accept: application/xml
HTTP/1.1 200 OK
Content-Type: application/xml
Content-Length: 152

<?xml version="1.0" encoding="UTF-8" standalone="yes"?><product id="4"><name>Teapot</name><sku>TP-01</sku><price>31.00</price><stock>5</stock></product>
$ curl -i -X POST localhost:8080/api/products -H Content-Type: application/xml -H Accept: application/xml -d <product><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/5
Content-Type: application/xml
Content-Length: 148

<?xml version="1.0" encoding="UTF-8" standalone="yes"?><product id="5"><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>
$ curl -i localhost:8080/api/products/5 -H Accept: application/xml
HTTP/1.1 200 OK
Content-Type: application/xml
Content-Length: 148

<?xml version="1.0" encoding="UTF-8" standalone="yes"?><product id="5"><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>
$ curl -i -X PATCH localhost:8080/api/products/3/stock -H Content-Type: application/json -d {"delta":-1}
HTTP/1.1 409 Conflict
Content-Length: 0


$ curl -i -X PATCH localhost:8080/api/products/2/stock -H Content-Type: application/json -d {"delta":-5}
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 59

{"id":2,"name":"Mug","sku":"MG-01","price":6.50,"stock":35}
$ curl -i -X PUT localhost:8080/api/products/4 -H Content-Type: application/json -d {"name":"Big Teapot","sku":"TP-01","price":35.00,"stock":7}
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 66

{"id":4,"name":"Big Teapot","sku":"TP-01","price":35.00,"stock":7}
$ curl -i -X DELETE localhost:8080/api/products/4
HTTP/1.1 204 No Content


$ curl -i -X DELETE localhost:8080/api/products/4
HTTP/1.1 404 Not Found
Content-Length: 0


$ curl -i -X POST localhost:8080/api/products -H Content-Type: text/plain -d Teapot
HTTP/1.1 415 Unsupported Media Type
Content-Length: 0


```
