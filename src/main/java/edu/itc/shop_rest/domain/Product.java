package edu.itc.shop_rest.domain;

import java.math.BigDecimal;

public record Product(long id, String name, String sku, BigDecimal price, int stock, int version) {
}