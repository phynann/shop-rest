#!/usr/bin/env bash
# Runs every request from Tasks 1-4 against the shop-rest server and
# prints PASS/FAIL for each one.
#
# Usage:  bash shop-rest-all-tests.sh
# Other server:  BASE_URL=http://localhost:9090 bash shop-rest-all-tests.sh
#
# Task 2 reads the created product's ID from the Location header and cleans
# up after itself, so it works with any seed data and can be re-run.
# Assumes product 2 (Mug) has stock >= 5 and product 3 (Tea) has stock 0.

BASE="${BASE_URL:-http://localhost:8080}"
KEY="demo-123"
TMP="$(mktemp -d)"
PASS=0
FAIL=0
trap 'rm -rf "$TMP"' EXIT

# t "name" expected_status curl-args...
# Optional env checks (set them on the line before the call):
#   HDR='regex'      a response header line must match   (CHECK is an alias)
#   BODY='regex'     the response body must match
#   NOT_BODY='regex' the response body must NOT match
#   SHOWBODY=0       do not print the response body
t() {
  local name="$1" expected="$2"
  shift 2
  local code ok=1 why="" hdr="${HDR:-$CHECK}"
  code=$(curl -s -o "$TMP/body" -D "$TMP/headers" -w '%{http_code}' "$@")

  if [[ "$code" != "$expected" ]]; then
    ok=0; why="expected $expected, got $code"
  fi
  if [[ -n "$hdr" ]] && ! grep -qiE -- "$hdr" "$TMP/headers"; then
    ok=0; why="${why:+$why; }missing header: $hdr"
  fi
  if [[ -n "$BODY" ]] && ! grep -qE -- "$BODY" "$TMP/body"; then
    ok=0; why="${why:+$why; }body does not contain: $BODY"
  fi
  if [[ -n "$NOT_BODY" ]] && grep -qE -- "$NOT_BODY" "$TMP/body"; then
    ok=0; why="${why:+$why; }body must not contain: $NOT_BODY"
  fi

  if [[ $ok -eq 1 ]]; then
    PASS=$((PASS + 1)); echo "PASS  $name  [$code]"
  else
    FAIL=$((FAIL + 1)); echo "FAIL  $name  [$why]"
  fi

  if [[ "${SHOWBODY:-1}" == "1" && -s "$TMP/body" ]]; then
    head -c 250 "$TMP/body" | tr -d '\r' | sed 's/^/        /'
    echo
  fi
}

section() { echo; echo "=== $1 ==="; }

# ID from the Location header of the last response
loc_id() { grep -i '^Location:' "$TMP/headers" | tr -d '\r' | sed 's#.*/##'; }
# Current stock of a product
stock_of() { curl -s "$BASE/api/products/$1" | grep -oE '"stock":-?[0-9]+' | cut -d: -f2; }

JSON=(-H "Content-Type: application/json")
AUTH=(-H "X-Api-Key: $KEY")

# ---------------------------------------------------------------
section "TASK 1: Basic Read Operations (GET)"
# ---------------------------------------------------------------
t "1.1 List all products"       200 "$BASE/api/products"
t "1.2 Get product by ID (Mug)" 200 "$BASE/api/products/2"
t "1.3 Product not found"       404 "$BASE/api/products/99"
t "1.4 Invalid non-numeric ID"  404 "$BASE/api/products/abc"

# ---------------------------------------------------------------
section "TASK 2: CRUD & Content Negotiation (JSON & XML)"
# ---------------------------------------------------------------
HDR='^Location:.*/api/products/[0-9]+' BODY='"sku":"TP-01"' NOT_BODY='"version"' \
t "2.1 Create Product (JSON) -> 201 + Location" 201 \
  -X POST "$BASE/api/products" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
ID="$(loc_id)"
echo "        (created product id: ${ID:-none})"

t "2.2 Duplicate SKU -> 409" 409 \
  -X POST "$BASE/api/products" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'

HDR='^Content-Type:.*xml' BODY="<product id=\"$ID\">" \
t "2.3 Read Product as XML -> 200" 200 \
  "$BASE/api/products/$ID" -H "Accept: application/xml"

HDR='^Location:.*/api/products/[0-9]+' \
t "2.4 Create Product with XML -> 201" 201 \
  -X POST "$BASE/api/products" \
  -H "Content-Type: application/xml" -H "Accept: application/xml" "${AUTH[@]}" \
  -d '<product><name>Cup</name><sku>CP-01</sku><price>3.00</price><stock>8</stock></product>'
CUP_ID="$(loc_id)"

BEFORE="$(stock_of 2)"
BODY="\"stock\":$((BEFORE - 5))" \
t "2.5 Adjust Stock (PATCH delta -5) -> 200, stock decreases" 200 \
  -X PATCH "$BASE/api/products/2/stock" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"delta": -5}'

t "2.6 Insufficient Stock (Tea, delta -1) -> 409" 409 \
  -X PATCH "$BASE/api/products/3/stock" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"delta": -1}'

BODY='"name":"Big Teapot"' NOT_BODY='"version"' \
t "2.7 Replace Product (PUT) -> 200" 200 \
  -X PUT "$BASE/api/products/$ID" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"name":"Big Teapot","sku":"TP-01","price":35.00,"stock":7}'

t "2.7b PUT missing product -> 404" 404 \
  -X PUT "$BASE/api/products/99999" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"name":"Ghost","sku":"GH-01","price":1.00,"stock":1}'

t "2.8 Delete Product -> 204" 204 \
  -X DELETE "$BASE/api/products/$ID" "${AUTH[@]}"

t "2.9 Delete again -> 404" 404 \
  -X DELETE "$BASE/api/products/$ID" "${AUTH[@]}"

t "2.10 Unsupported Media Type (text/plain) -> 415" 415 \
  -X POST "$BASE/api/products" -H "Content-Type: text/plain" "${AUTH[@]}" -d 'Teapot'

# Cleanup so the script can be re-run without restarting the server
[[ -n "$CUP_ID" ]] && curl -s -o /dev/null -X DELETE "$BASE/api/products/$CUP_ID" "${AUTH[@]}"
curl -s -o /dev/null -X PATCH "$BASE/api/products/2/stock" "${JSON[@]}" "${AUTH[@]}" -d '{"delta": 5}'

# ---------------------------------------------------------------
section "TASK 3: Query Filtering, Sorting & Bean Validation"
# ---------------------------------------------------------------
t "3.1 Search keyword q"   200 "$BASE/api/products?q=tea"
t "3.2 Price range"        200 "$BASE/api/products?minPrice=5&maxPrice=30"
t "3.3 In-stock only"      200 "$BASE/api/products?inStock=true"
t "3.4 Sort by price desc" 200 "$BASE/api/products?sort=price,desc"

t "3.5 Bean Validation (bad.json)" 400 -X POST "$BASE/api/products" "${JSON[@]}" "${AUTH[@]}" \
  -d '{"name":"","sku":"kettle","price":0,"stock":-1}'

# ---------------------------------------------------------------
section "TASK 4: Security, CORS & Interceptors"
# ---------------------------------------------------------------
t "4.1 POST without API Key" 401 -X POST "$BASE/api/products" "${JSON[@]}" \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'

t "4.2 POST with invalid key" 401 -X POST "$BASE/api/products" "${JSON[@]}" \
  -H "X-Api-Key: wrong-key" \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'

HDR='^Access-Control-Allow-' \
t "4.3 CORS Preflight check" 200 -X OPTIONS "$BASE/api/products" \
  -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: POST"

HDR='^X-Correlation-Id: order-check-42' \
t "4.4 Echo Correlation ID" 200 "$BASE/api/products/1" -H "X-Correlation-Id: order-check-42"

HDR='^Content-Encoding: gzip' SHOWBODY=0 \
t "4.5 GZIP Compression" 200 "$BASE/api/products" -H "Accept-Encoding: gzip"

# ---------------------------------------------------------------
echo
echo "==============================="
echo "  PASSED: $PASS    FAILED: $FAIL"
echo "==============================="
[[ $FAIL -eq 0 ]]
