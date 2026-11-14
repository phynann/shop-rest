package edu.itc.shop_rest.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {
    public Money {
        amount = Objects.requireNonNull(amount);
        currency = Objects.requireNonNull(currency);
    }
}