package edu.itc.shop_rest.api;

import edu.itc.shop_rest.domain.Money;

public record ProductV2(long id, String name, String sku, Money price, int stock, boolean available) {
}